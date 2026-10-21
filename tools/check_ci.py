import urllib.request
import json
import time
import sys
import os

REPO = "azimuddinkamal/cleanyourtv"
HEADERS = {"User-Agent": "Mozilla/5.0"}

def get_latest_run():
    url = f"https://api.github.com/repos/{REPO}/actions/runs?per_page=5"
    req = urllib.request.Request(url, headers=HEADERS)
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        runs = data.get("workflow_runs", [])
        return runs

def wait_for_run(expected_sha):
    print(f"Monitoring GitHub Actions for SHA {expected_sha}...")
    start_time = time.time()
    while time.time() - start_time < 600:
        runs = get_latest_run()
        if runs:
            latest = runs[0]
            sha = latest["head_sha"][:7]
            status = latest["status"]
            conclusion = latest.get("conclusion")
            print(f"[{int(time.time() - start_time)}s] ID: {latest['id']}, Status: {status}, Conclusion: {conclusion}, SHA: {sha}")
            if status == "completed":
                return conclusion == "success"
        time.sleep(15)
    return False

def download_release_apk(dest_path="app-release.apk"):
    url = f"https://github.com/{REPO}/releases/download/v2.5-release/app-release.apk"
    print(f"Downloading release APK from {url} to {dest_path}...")
    req = urllib.request.Request(url, headers=HEADERS)
    # Follow redirects
    with urllib.request.urlopen(req) as resp, open(dest_path, "wb") as out_file:
        out_file.write(resp.read())
    size = os.path.getsize(dest_path)
    print(f"Downloaded APK successfully! Size: {size / 1024:.1f} KB")

if __name__ == "__main__":
    expected_sha = sys.argv[1] if len(sys.argv) > 1 else "355106c"
    success = wait_for_run(expected_sha)
    if success:
        print("CI completed successfully! Downloading release APK...")
        time.sleep(5)
        download_release_apk("app-release.apk")
    else:
        print("CI failed or timed out!")
        sys.exit(1)
