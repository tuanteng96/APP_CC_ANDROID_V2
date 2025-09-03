package vn.cser21.incoming;

public class IncomingEvent {
    public String url;
    public int id;

    public IncomingEvent(String url, int id) {
        this.url = url;
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
