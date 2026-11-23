def get_dashboard():
    return {
        "name": "KARA OMEGA",
        "version": "1.0",
        "engine": "Python 3.10",
        "status": "READY",
        "total": 0,
        "valid": 0,
        "invalid": 0
    }


def parse_m3u(content):
    results = []
    current_name = "Unknown"

    for line in content.splitlines():
        line = line.strip()

        if line.startswith("#EXTINF:"):
            if "," in line:
                current_name = line.split(",", 1)[1].strip()
            else:
                current_name = "Unknown"

        elif line and not line.startswith("#"):
            results.append({
                "name": current_name,
                "url": line
            })
            current_name = "Unknown"

    return results
