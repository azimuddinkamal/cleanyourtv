import os
import sys
import time
import subprocess

TV_HOST = "192.168.1.3"
os.environ["TV_HOST"] = TV_HOST

PYTHON_BIN = "tools/venv/Scripts/python.exe" if os.path.exists("tools/venv/Scripts/python.exe") else sys.executable

def run_tv(cmd):
    print(f">> TV: {cmd}")
    res = subprocess.run([PYTHON_BIN, "tools/tv.py", cmd], capture_output=True, text=True)
    if res.stdout:
        print(res.stdout.strip())
    if res.stderr:
        print("STDERR:", res.stderr.strip())
    return res.returncode == 0

def push_apk(local_apk):
    print(f"Pushing {local_apk} via b64push...")
    res = subprocess.run([PYTHON_BIN, "tools/b64push.py", local_apk, "/data/local/tmp/tvhome.apk"], capture_output=True, text=True)
    print(res.stdout.strip())
    return res.returncode == 0

def take_screenshot(filename):
    print(f"Taking screenshot -> {filename}...")
    res = subprocess.run([PYTHON_BIN, "tools/tv.py", "--screenshot", filename], capture_output=True, text=True)
    print(res.stdout.strip())

def main():
    if not os.path.exists("app-release.apk"):
        print("Error: app-release.apk not found!")
        sys.exit(1)

    print("--- 1. PUSHING APK ---")
    if not push_apk("app-release.apk"):
        print("Failed to push APK")
        sys.exit(1)

    print("--- 2. INSTALLING & SETTING DEFAULT HOME ---")
    install_cmd = "pm uninstall com.gathin.tvhome && pm install /data/local/tmp/tvhome.apk && cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity && am start -W -n com.gathin.tvhome/.MainActivity && rm -f /data/local/tmp/tvhome.apk"
    run_tv(install_cmd)

    print("--- 3. GRANTING PERMISSIONS & SPEED COMPILATION ---")
    run_tv("pm grant com.gathin.tvhome android.permission.ACCESS_FINE_LOCATION")
    run_tv("pm grant com.gathin.tvhome android.permission.ACCESS_COARSE_LOCATION")
    run_tv("cmd package compile -m speed -f com.gathin.tvhome")

    print("--- 4. APPLYING AZIM-CLEAN THEME ---")
    subprocess.run(["powershell", "-ExecutionPolicy", "Bypass", "-File", "tools/theme.ps1", "-TvHost", TV_HOST, "themes/azim-clean"])

    print("--- 5. RE-LAUNCHING CLEAN YOUR TV ---")
    run_tv("am start -W -n com.gathin.tvhome/.MainActivity")
    time.sleep(3)

    print("--- 6. VERIFICATION SCREENSHOTS ---")
    take_screenshot("shot_home.png")

    # Focus through apps and capture screenshots
    # 1. YouTube
    run_tv("input keyevent 3") # HOME
    time.sleep(1)
    run_tv("input keyevent 20") # DPAD_DOWN to dock
    time.sleep(1)
    take_screenshot("shot_dock_app1.png")

    # 2. Next app (Netflix)
    run_tv("input keyevent 22") # DPAD_RIGHT
    time.sleep(1)
    take_screenshot("shot_dock_app2.png")

    # 3. Next app (Durioo+)
    run_tv("input keyevent 22") # DPAD_RIGHT
    time.sleep(1)
    take_screenshot("shot_dock_app3.png")

    # 4. Next app (Disney+)
    run_tv("input keyevent 22") # DPAD_RIGHT
    time.sleep(1)
    take_screenshot("shot_dock_app4.png")

    # 5. Next app (Apple TV)
    run_tv("input keyevent 22") # DPAD_RIGHT
    time.sleep(1)
    take_screenshot("shot_dock_app5.png")

    print("ALL DONE! Screenshots captured.")

if __name__ == "__main__":
    main()
