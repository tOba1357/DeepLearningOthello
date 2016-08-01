package utils;

public enum FileName {
    SAVE_FILE("data.ckpt"),
    SAVE_BLACK_FILE("black_data.ckpt"),
    SAVE_WHITE_FILE("white_data.ckpt");

    private final String fileName;


    FileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
