package Launcher;

public class VersionEntry implements Comparable<VersionEntry> {
    private final String id;
    private final String type;
    private final String url;
    private final String time;
    private final String releaseTime;

    public VersionEntry(String id, String url, String type, String time, String releaseTime) {
        this.id = id;
        this.url = url;
        this.type = type;
        this.time = time;
        this.releaseTime = releaseTime;
    }

    public  String getId() {
        return this.id;
    }

    public String getDownloadUrl() {
        return this.url;
    }

    public String getType() {
        return this.type;
    }

    public String getTime() { return this.time; }

    public String getReleaseTime() { return this.releaseTime; }


    @Override
    public String toString() {
        return "Version: " + id + " type: " + type;
    }

    @Override
    public int compareTo(VersionEntry entry) {
        if(entry.id.equals(this.id)) {
            if(entry.type.equals(this.type))
                return 0;
            if(entry.type.equals("snapshot")) {
                if(this.type.equals("release")) {
                    return -1;
                }
                return 1;
            } else if(entry.type.equals("release")) {
                return 1;
            }
            return this.type.compareTo(entry.type);
        }
        return 0;
    }
}
