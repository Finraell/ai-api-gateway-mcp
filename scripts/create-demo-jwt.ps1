param(
  [string]$Subject = "demo-recruiter-client",
  [string]$Secret = "replace-with-at-least-32-random-characters"
)

function Base64UrlEncode([byte[]]$bytes) {
  [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+','-').Replace('/','_')
}

$header = @{ alg = "HS256"; typ = "JWT" } | ConvertTo-Json -Compress
$payload = @{
  sub = $Subject
  iat = [int][double]::Parse((Get-Date -UFormat %s))
  exp = [int][double]::Parse((Get-Date).AddHours(1).ToUniversalTime().Subtract([datetime]'1970-01-01').TotalSeconds)
  scope = "risk:score mcp:tools"
} | ConvertTo-Json -Compress

$headerPart = Base64UrlEncode([Text.Encoding]::UTF8.GetBytes($header))
$payloadPart = Base64UrlEncode([Text.Encoding]::UTF8.GetBytes($payload))
$signingInput = "$headerPart.$payloadPart"
$hmac = [System.Security.Cryptography.HMACSHA256]::new([Text.Encoding]::UTF8.GetBytes($Secret))
$signaturePart = Base64UrlEncode($hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($signingInput)))
"$signingInput.$signaturePart"
