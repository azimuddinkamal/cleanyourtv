$lines = "protocol=https`nhost=github.com" | git credential fill
$tok = ($lines | Where-Object { $_ -match '^password=(.*)' } | ForEach-Object { $Matches[1] })
$headers = @{
    'Authorization' = "Bearer $tok"
    'User-Agent' = 'PowerShell'
}
Invoke-RestMethod -Uri 'https://api.github.com/repos/azimuddinkamal/cleanyourtv/actions/jobs/119294329310/logs' -Headers $headers -OutFile 'backup/job_build_fail.log'
Get-Content 'backup/job_build_fail.log' | Select-String -Pattern 'error:|failure|FAILED' -Context 3,3 | Select-Object -Last 10
