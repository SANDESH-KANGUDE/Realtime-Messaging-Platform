# PowerShell script to start all services with optimized memory footprint

# Load environment variables from .env if present
if (Test-Path ".env") {
    Write-Host "Loading environment variables from root .env..." -ForegroundColor Yellow
    Get-Content ".env" | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $key, $value = $line.Split("=", 2)
            $key = $key.Trim()
            $value = $value.Trim()
            if (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'"))) {
                $value = $value.Substring(1, $value.Length - 2)
            }
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        }
    }
}

Write-Host "Starting Docker containers..." -ForegroundColor Green
docker-compose up -d

# Give Docker services 10 seconds to warm up
Write-Host "Waiting 10 seconds for databases and Kafka to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "Launching 11 Backend Services..." -ForegroundColor Green
$services = @(
    "gateway-service",
    "auth-service",
    "user-service",
    "chat-service",
    "message-service",
    "realtime-service",
    "notification-service",
    "media-service",
    "search-service",
    "ai-service"
)

foreach ($service in $services) {
    Write-Host "Starting $service..." -ForegroundColor Cyan
    # Launch native PowerShell windows, setting the WindowTitle dynamically using native PowerShell syntax
    # Note: Memory optimization (128M limit) is configured in backend/pom.xml under spring-boot-maven-plugin
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$Host.UI.RawUI.WindowTitle = '$service'; & 'C:\Users\sande\.maven\apache-maven-3.9.6\bin\mvn.cmd' spring-boot:run -f backend/$service/pom.xml"
}

Write-Host "Starting Frontend UI..." -ForegroundColor Green
# Launch Frontend in native PowerShell window
Start-Process powershell -ArgumentList "-NoExit", "-Command", "`$Host.UI.RawUI.WindowTitle = 'frontend'; cd frontend; npm run dev"

Write-Host "All processes launched successfully! You can close this supervisor terminal." -ForegroundColor Yellow
