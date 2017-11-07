package net.fishtron.utils;

import java.util.function.Consumer;
import java.util.function.Function;

/** Created by Tomáš Křen on 26.12.2016. */

public interface Either<A,B> {

    boolean isOK();
    A getOK();
    B getKO();

    void ifOK(Consumer<A> okCase, Consumer<B> koCase);
    <C> C ifOK(Function<A,C> okCase, Function<B,C> koCase);
    B ifOK(Function<A,B> okCase);

    default boolean isKO() {
        return !isOK();
    }

    default <C> Either<C,B> ifOK2(Function<A,C> okCase) {
        if (isOK()) {
            return ok(okCase.apply(getOK()));
        } else {
            return ko(getKO());
        }
    }

    default <C> Either<C,B> bind(Function<A,Either<C,B>> f) {
        if (isOK()) {
            return f.apply(getOK());
        } else {
            return ko(getKO());
        }
    }

    static <X,Y> Either<X,Y> ok(X okVal) {
        return new OK<>(okVal);
    }

    static <X,Y> Either<X,Y> ko(Y koVal) {
        return new KO<>(koVal);
    }

    class OK<X,Y> implements Either<X,Y> {
        private X okVal;
        private OK(X okVal) {this.okVal = okVal;}
        @Override public boolean isOK() {return true;}
        @Override public X getOK() {return okVal;}
        @Override public Y getKO() {return null;}
        @Override public void ifOK(Consumer<X> okCase, Consumer<Y> koCase) {okCase.accept(okVal);}
        @Override public <C> C ifOK(Function<X, C> okCase, Function<Y, C> koCase) {return okCase.apply(okVal);}
        @Override public Y ifOK(Function<X, Y> okCase) {return okCase.apply(okVal);}
    }

    class KO<X,Y> implements Either<X,Y> {
        private Y koVal;
        private KO(Y koVal) {this.koVal = koVal;}
        @Override public boolean isOK() {return false;}
        @Override public X getOK() {return null;}
        @Override public Y getKO() {return koVal;}
        @Override public void ifOK(Consumer<X> okCase, Consumer<Y> koCase) {koCase.accept(koVal);}
        @Override public <C> C ifOK(Function<X, C> okCase, Function<Y, C> koCase) {return koCase.apply(koVal);}
        @Override public Y ifOK(Function<X, Y> okCase) {return koVal;}
    }

}
