import json

def get_dashboard():
    return json.dumps({
        "version": "2.0",
        "engine": "Python",
        "status": "READY",
        "total": 0,
        "valid": 0,
        "invalid": 0
    })

def parse_m3u(content):
    channels = []
    current = {
        "name": "Unknown",
        "group": "Uncategorized",
        "logo": ""
    }

    for raw in content.splitlines():
        line = raw.strip()

        if not line:
            continue

        if line.startswith("#EXTINF:"):
            current = {
                "name": line.split(",", 1)[-1].strip()
                        if "," in line else "Unknown",
                "group": "Uncategorized",
                "logo": ""
            }

            for part in line.split():
                if part.startswith('group-title="'):
                    current["group"] = part.split('"')[1]
                elif part.startswith('tvg-logo="'):
                    current["logo"] = part.split('"')[1]

        elif line.startswith("#EXTGRP:"):
            current["group"] = line.split(":", 1)[1].strip()

        elif not line.startswith("#"):
            channels.append({
                "name": current["name"],
                "group": current["group"],
                "logo": current["logo"],
                "url": line
            })

            current = {
                "name": "Unknown",
                "group": "Uncategorized",
                "logo": ""
            }

    return json.dumps({
        "total": len(channels),
        "channels": channels
    }, ensure_ascii=False)

def search_channels(content, query):
    data = json.loads(parse_m3u(content))
    q = query.lower()

    results = [
        item for item in data["channels"]
        if q in item["name"].lower()
        or q in item["group"].lower()
    ]

    return json.dumps(results, ensure_ascii=False)
