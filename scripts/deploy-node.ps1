# ==============================================================================
# Workstation Deployment Script: Deploy Release to Target Node via Ansible (PowerShell)
# Project: Construction Progress Dashboard
# Milestone: Week 15 Final End-to-End Release
# ==============================================================================

param (
    [string]$ReleaseTag = "",
    [string]$ImageRepo = "localhost:5000/construction-dashboard",
    [string]$TargetHost = "127.0.0.1",
    [string]$TargetPort = "8082"
)

$ErrorActionPreference = "Stop"
$workspaceRoot = (Resolve-Path "$PSScriptRoot\..").Path
Set-Location $workspaceRoot

# Determine Version and Release Tag if not specified
if (-not $ReleaseTag) {
    [xml]$pom = Get-Content "$workspaceRoot\pom.xml"
    $appVersion = $pom.project.version
    if (-not $appVersion) { $appVersion = "1.0.0" }
    $shortSha = (git.exe rev-parse --short=7 HEAD).Trim()
    $ReleaseTag = "${appVersion}-b1-${shortSha}"
}

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "DEPLOYING RELEASE VIA ANSIBLE WORKSTATION RUNNER (PowerShell)" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "Workspace:    $workspaceRoot"
Write-Host "Release Tag:  $ReleaseTag"
Write-Host "Image Repo:   $ImageRepo"
Write-Host "Target Host:  ${TargetHost}:${TargetPort}"
Write-Host "================================================================================" -ForegroundColor Cyan

# Check if ansible-playbook is installed natively or if WSL must be used
$hasNativeAnsible = (Get-Command ansible-playbook -ErrorAction SilentlyContinue)

if ($hasNativeAnsible) {
    Write-Host "Executing via native Ansible CLI..." -ForegroundColor Green
    $env:ANSIBLE_CONFIG = "ansible/ansible.cfg"
    $env:ANSIBLE_ROLES_PATH = "ansible/roles"
    $env:ANSIBLE_HOST_KEY_CHECKING = "False"
    ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml `
        -e "release_tag=$ReleaseTag" -e "docker_image=$ImageRepo" | Tee-Object -FilePath ansible-deploy.log
    ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/healthcheck.yml | Tee-Object -FilePath ansible-healthcheck.log
} else {
    Write-Host "Native ansible-playbook not detected; delegating to WSL Ubuntu distribution..." -ForegroundColor Yellow
    $wslPath = ($workspaceRoot -replace '\\', '/').ToLower()
    $drive = $wslPath.Substring(0, 1)
    $rest = $wslPath.Substring(2)
    $wslWorkspace = "/mnt/$drive$rest"
    $envPrefix = "ANSIBLE_CONFIG=ansible/ansible.cfg ANSIBLE_ROLES_PATH=ansible/roles ANSIBLE_HOST_KEY_CHECKING=False"
    
    $deployCmd = "cd $wslWorkspace && $envPrefix ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/provision.yml -e release_tag=$ReleaseTag -e docker_image=$ImageRepo"
    wsl.exe -d Ubuntu -u ganesh -- bash -c "$deployCmd" | Tee-Object -FilePath ansible-deploy.log

    $hcCmd = "cd $wslWorkspace && $envPrefix ansible-playbook -i ansible/inventory/hosts.yml ansible/playbooks/healthcheck.yml"
    wsl.exe -d Ubuntu -u ganesh -- bash -c "$hcCmd" | Tee-Object -FilePath ansible-healthcheck.log
}

# 3. Workstation HTTP Verification
Write-Host "`n>>> Performing HTTP Verification from Workstation..." -ForegroundColor Cyan
$healthUrl = "http://${TargetHost}:${TargetPort}/health"
$loginUrl = "http://${TargetHost}:${TargetPort}/login"

$healthResp = curl.exe -s -m 5 "$healthUrl"
Write-Host "Health Check Response: $healthResp"

if ($healthResp -like "*UP*") {
    Write-Host "✓ Health Check Endpoint returned UP" -ForegroundColor Green
} else {
    Write-Error "Health check failed at $healthUrl"
}

$loginHtml = curl.exe -s -m 5 "$loginUrl"
if ($loginHtml -like "*$ReleaseTag*") {
    Write-Host "✓ Login page contains expected release version tag '$ReleaseTag'" -ForegroundColor Green
} else {
    Write-Warning "Release tag '$ReleaseTag' not found in login page HTML"
}

# 4. Generate deploy-info.txt
$commitSha = (git.exe rev-parse --short=7 HEAD).Trim()
$timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-dd HH:mm:ss UTC")
$deployInfo = @"
Deployment Information - Week 15 Final Release
-----------------------------------------------------------
Application Name: Construction Progress Dashboard
Deploy Target:    ansible
Registry Mode:    $ImageRepo
Image Repository: $ImageRepo
Image Tag:        $ReleaseTag
Git Commit:       $commitSha
Target Node URL:  http://${TargetHost}:${TargetPort}
Health URL:       $healthUrl
Timestamp:        $timestamp
"@

Set-Content -Path "deploy-info.txt" -Value $deployInfo
Write-Host "`n================================================================================" -ForegroundColor Cyan
Write-Host "DEPLOYMENT AND VERIFICATION COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "Deploy info written to deploy-info.txt" -ForegroundColor Green
Write-Host "================================================================================" -ForegroundColor Cyan
