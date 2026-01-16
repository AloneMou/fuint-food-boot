# OpenOrderController 测试运行脚本
# 使用PowerShell运行此脚本

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("all", "unit", "integration", "single")]
    [string]$TestType = "all",
    
    [Parameter(Mandatory=$false)]
    [string]$TestMethod = ""
)

# 设置控制台输出颜色
$ErrorActionPreference = "Continue"

# 项目根目录
$ProjectRoot = "d:\Project\Aite\fuint-food-boot"
$ModulePath = "$ProjectRoot\fuint-application"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  OpenOrderController 测试执行工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Maven是否安装
function Test-Maven {
    try {
        $mvnVersion = mvn -v 2>&1 | Select-Object -First 1
        Write-Host "✓ Maven 检测成功: $mvnVersion" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "✗ 未检测到Maven，请先安装Maven并配置环境变量" -ForegroundColor Red
        return $false
    }
}

# 运行测试
function Invoke-Test {
    param(
        [string]$TestClass,
        [string]$Method = ""
    )
    
    Set-Location $ProjectRoot
    
    $testCommand = "mvn test -f $ModulePath\pom.xml"
    
    if ($TestClass) {
        if ($Method) {
            $testCommand += " -Dtest=$TestClass#$Method"
            Write-Host "运行测试方法: $TestClass#$Method" -ForegroundColor Yellow
        }
        else {
            $testCommand += " -Dtest=$TestClass"
            Write-Host "运行测试类: $TestClass" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "运行所有测试..." -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "执行命令: $testCommand" -ForegroundColor Gray
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host ""
    
    # 执行测试
    Invoke-Expression $testCommand
    
    return $LASTEXITCODE
}

# 显示测试报告位置
function Show-TestReport {
    $reportPath = "$ModulePath\target\surefire-reports"
    
    if (Test-Path $reportPath) {
        Write-Host ""
        Write-Host "----------------------------------------" -ForegroundColor Gray
        Write-Host "测试报告位置: $reportPath" -ForegroundColor Cyan
        
        $xmlReports = Get-ChildItem -Path $reportPath -Filter "TEST-*.xml"
        if ($xmlReports.Count -gt 0) {
            Write-Host "共生成 $($xmlReports.Count) 个测试报告文件" -ForegroundColor Cyan
        }
    }
}

# 主执行逻辑
if (-not (Test-Maven)) {
    exit 1
}

Write-Host ""
Write-Host "项目路径: $ProjectRoot" -ForegroundColor Gray
Write-Host "模块路径: $ModulePath" -ForegroundColor Gray
Write-Host ""

$exitCode = 0

switch ($TestType) {
    "all" {
        Write-Host "执行所有测试用例" -ForegroundColor Cyan
        Write-Host ""
        $exitCode = Invoke-Test
    }
    "unit" {
        Write-Host "执行单元测试" -ForegroundColor Cyan
        Write-Host ""
        $exitCode = Invoke-Test -TestClass "OpenOrderControllerTest"
    }
    "integration" {
        Write-Host "执行集成测试" -ForegroundColor Cyan
        Write-Host ""
        $exitCode = Invoke-Test -TestClass "OpenOrderControllerIntegrationTest"
    }
    "single" {
        if ([string]::IsNullOrEmpty($TestMethod)) {
            Write-Host "错误: 执行单个测试方法需要指定 -TestMethod 参数" -ForegroundColor Red
            Write-Host "示例: .\run-order-tests.ps1 -TestType single -TestMethod testPreCreateOrder_Success" -ForegroundColor Yellow
            exit 1
        }
        Write-Host "执行单个测试方法" -ForegroundColor Cyan
        Write-Host ""
        $exitCode = Invoke-Test -TestClass "OpenOrderControllerTest" -Method $TestMethod
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Gray

# 显示测试结果
if ($exitCode -eq 0) {
    Write-Host "✓ 测试执行成功！" -ForegroundColor Green
} else {
    Write-Host "✗ 测试执行失败，请查看详细日志" -ForegroundColor Red
}

Show-TestReport

Write-Host "========================================" -ForegroundColor Gray
Write-Host ""

# 询问是否生成HTML报告
$generateReport = Read-Host "是否生成HTML测试报告？(y/n)"
if ($generateReport -eq "y" -or $generateReport -eq "Y") {
    Write-Host ""
    Write-Host "正在生成HTML报告..." -ForegroundColor Yellow
    Set-Location $ProjectRoot
    mvn surefire-report:report -f $ModulePath\pom.xml
    
    $htmlReport = "$ModulePath\target\site\surefire-report.html"
    if (Test-Path $htmlReport) {
        Write-Host "✓ HTML报告生成成功: $htmlReport" -ForegroundColor Green
        
        $openReport = Read-Host "是否打开HTML报告？(y/n)"
        if ($openReport -eq "y" -or $openReport -eq "Y") {
            Start-Process $htmlReport
        }
    }
    else {
        Write-Host "✗ HTML报告生成失败" -ForegroundColor Red
    }
}

exit $exitCode
