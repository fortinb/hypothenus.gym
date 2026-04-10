$file = 'D:\isoceles\dev\hypothenus.gym\proj\hypo.admin.papi\src\test\java\com\iso\hypo\tests\data\Populator.java'
$content = Get-Content $file -Raw
$result = [regex]::Replace($content, '(?s)(buildTermsOfUse\()(.+?)(\))', {
    param($m)
    $m.Groups[1].Value + $m.Groups[2].Value.Replace('.', ';') + $m.Groups[3].Value
})
Set-Content $file $result -NoNewline
Write-Host 'Done'
