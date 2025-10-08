# Script for generating base64 keystore code for GitHub Secrets
# Usage: .\scripts\generate-keystore-base64.ps1

Write-Host "Generating base64 code for keystore..." -ForegroundColor Green

$keystorePath = "keystore\adygyes-release.keystore"

# Check if file exists
if (-not (Test-Path $keystorePath)) {
    Write-Host "ERROR: Keystore file not found: $keystorePath" -ForegroundColor Red
    Write-Host "Make sure the file exists and path is correct" -ForegroundColor Yellow
    exit 1
}

# Get file information
$fileInfo = Get-Item $keystorePath
Write-Host "File found: $($fileInfo.Name)" -ForegroundColor Cyan
Write-Host "File size: $($fileInfo.Length) bytes" -ForegroundColor Cyan

try {
    # Read file as bytes and convert to base64
    $keystoreBytes = [System.IO.File]::ReadAllBytes($keystorePath)
    $base64String = [System.Convert]::ToBase64String($keystoreBytes)
    
    Write-Host "SUCCESS: Base64 code generated!" -ForegroundColor Green
    Write-Host "Base64 string length: $($base64String.Length) characters" -ForegroundColor Cyan
    
    # Save to file for convenience
    $outputFile = "keystore-base64.txt"
    $base64String | Out-File -FilePath $outputFile -Encoding UTF8
    
    Write-Host "" -ForegroundColor White
    Write-Host "INSTRUCTIONS:" -ForegroundColor Yellow
    Write-Host "1. Copy content of file: $outputFile" -ForegroundColor White
    Write-Host "2. Go to GitHub -> Settings -> Secrets and variables -> Actions" -ForegroundColor White
    Write-Host "3. Create new secret: KEYSTORE_BASE64" -ForegroundColor White
    Write-Host "4. Paste the copied base64 code" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    
    # Show preview for verification
    $preview = $base64String.Substring(0, [Math]::Min(50, $base64String.Length))
    $suffix = if ($base64String.Length -gt 50) { "..." + $base64String.Substring($base64String.Length - 10) } else { "" }
    
    Write-Host "Base64 code preview:" -ForegroundColor Magenta
    Write-Host "$preview$suffix" -ForegroundColor Gray
    
    Write-Host "" -ForegroundColor White
    Write-Host "WARNING: Never publish this base64 code publicly!" -ForegroundColor Red
    Write-Host "SUCCESS: File $outputFile created for copying to GitHub Secrets" -ForegroundColor Green
    
} catch {
    Write-Host "ERROR generating base64: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "" -ForegroundColor White
Write-Host "DONE! Now add the secret to GitHub and retry the release." -ForegroundColor Green