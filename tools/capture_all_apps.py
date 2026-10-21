import os
import sys
import time
import subprocess

TV_HOST = "192.168.1.3"
os.environ["TV_HOST"] = TV_HOST
PYTHON_BIN = "tools/venv/Scripts/python.exe" if os.path.exists("tools/venv/Scripts/python.exe") else sys.executable

def run_tv(cmd):
    res = subprocess.run([PYTHON_BIN, "tools/tv.py", cmd], capture_output=True, text=True)
    return res.stdout.strip()

def screenshot(fn):
    print(f"Capturing {fn}...")
    subprocess.run([PYTHON_BIN, "tools/tv.py", "--screenshot", fn], capture_output=True, text=True)

def main():
    print("Navigating to home...")
    run_tv("input keyevent 3")
    time.sleep(1.5)

    # Down to dock
    run_tv("input keyevent 20")
    time.sleep(1.5)

    apps = ["youtube", "netflix", "durioo", "disney", "appletv"]
    for i, app in enumerate(apps):
        fn = f"shot_verified_{app}.png"
        time.sleep(1.2)
        screenshot(fn)
        if i < len(apps) - 1:
            run_tv("input keyevent 22")
            time.sleep(1.2)

    print("Finished capturing all 5 apps!")

if __name__ == "__main__":
    main()
