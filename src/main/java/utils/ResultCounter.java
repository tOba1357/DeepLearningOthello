package utils;

import game.Object.Winner;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tatsuya Oba
 */
public class ResultCounter {
    private final AtomicInteger blackWinCounter;
    private final AtomicInteger whiteWinCounter;
    private final AtomicInteger drawCounter;


    public ResultCounter() {
        this.blackWinCounter = new AtomicInteger();
        this.whiteWinCounter = new AtomicInteger();
        this.drawCounter = new AtomicInteger();
    }

    public int increment(final Winner winner) {
        return getCounter(winner).incrementAndGet();
    }

    public int get(final Winner winner) {
        return getCounter(winner).get();
    }

    public AtomicInteger getCounter(final Winner winner) {
        switch (winner) {
            case BLACK:
                return blackWinCounter;
            case WHITE:
                return whiteWinCounter;
            case DRAW:
                return drawCounter;
        }
        throw new IllegalArgumentException("not find matching counter");
    }

    @Override
    public String toString() {
        return "black/white/draw\n" +
                blackWinCounter.get() + "/" +
                whiteWinCounter.get() + "/" +
                drawCounter.get() + "\n";
    }
}
