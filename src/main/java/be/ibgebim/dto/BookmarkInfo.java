package be.ibgebim.dto;

public class BookmarkInfo {
    float xPos;
    float yPos;
    int page = 1;
    boolean bookmarkPresent;
    boolean bookmarkFilled;

    public BookmarkInfo() {
        super();

        xPos = 0;
        yPos = 0;
        page = 1;
        bookmarkPresent = false;
        bookmarkFilled = false;
    }

    public BookmarkInfo(float xPos, float yPos, int page, boolean bookmarkPresent) {
        super();
        this.xPos = xPos;
        this.yPos = yPos;
        this.page = page;
        this.bookmarkPresent = bookmarkPresent;
        this.bookmarkFilled = false;
    }

    public BookmarkInfo(float xPos, float yPos, int page, boolean bookmarkPresent, boolean bookmarkFilled) {
        super();
        this.xPos = xPos;
        this.yPos = yPos;
        this.page = page;
        this.bookmarkPresent = bookmarkPresent;
        this.bookmarkFilled = bookmarkFilled;
    }

    public float getxPos() {
        return xPos;
    }

    public void setxPos(float xPos) {
        this.xPos = xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public void setyPos(float yPos) {
        this.yPos = yPos;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isBookmarkPresent() {
        return bookmarkPresent;
    }

    public void setBookmarkPresent(boolean bookmarkPresent) {
        this.bookmarkPresent = bookmarkPresent;
    }

    public boolean isBookmarkFilled() { return bookmarkFilled; }

    public void setBookmarkFilled(boolean bookmarkFilled) { this.bookmarkFilled = bookmarkFilled; }

    @Override
    public String toString() {
        return "BookmarkInfo{" +
                "xPos=" + xPos +
                ", yPos=" + yPos +
                ", page=" + page +
                ", bookmarkPresent=" + bookmarkPresent +
                ", bookmarkFilled=" + bookmarkFilled +
                '}';
    }
}
