package utils;

public enum FileName {
    SAVE_FILE("data.ckpt");

    private final String fileName;


    FileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
