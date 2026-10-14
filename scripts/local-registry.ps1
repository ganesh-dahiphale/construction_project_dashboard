# ========================================================
# Start Local Docker Registry (registry:2 on localhost:5000)
# ========================================================
$ErrorActionPreference = "Continue"

$registryName = "registry"
$registryPort = 5000

Write-Host "Checking if local registry container '$registryName' is running on port $registryPort..."

$running = docker ps -q -f "name=^/${registryName}$"
if ($running) {
    Write-Host "Local registry '$registryName' is already running."
} else {
    $exists = docker ps -aq -f "name=^/${registryName}$"
    if ($exists) {
        Write-Host "Local registry container exists but is stopped. Starting '$registryName'..."
        docker start $registryName
    } else {
        Write-Host "Starting fresh local registry container '$registryName' on port $registryPort..."
        docker run -d -p "${registryPort}:5000" --restart unless-stopped --name $registryName registry:2
    }
}

Write-Host "Verifying local registry status..."
docker ps --filter "name=^/${registryName}$"
try {
    $res = Invoke-RestMethod -Uri "http://localhost:${registryPort}/v2/" -Method Get -ErrorAction SilentlyContinue
    Write-Host "Local Docker Registry v2 API is reachable at http://localhost:${registryPort}/v2/"
} catch {
    Write-Host "Local Docker Registry responded with: $($_.Exception.Message)"
}
