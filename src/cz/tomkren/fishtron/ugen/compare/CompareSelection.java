package cz.tomkren.fishtron.ugen.compare;

import cz.tomkren.fishtron.ugen.multi.MultiIndiv;
import net.fishtron.utils.F;

import java.util.Collection;
import java.util.Random;
import java.util.function.BiFunction;

/**Created by tom on 22.03.2017.*/

public interface CompareSelection<Indiv extends MultiIndiv> {

    Indiv select(Collection<Indiv> pop, BiFunction<Indiv,Indiv,Boolean> compareFun);

    class Tournament<T extends MultiIndiv> implements CompareSelection<T> {

        private final double pReturnWinner;
        private final Random rand;

        public Tournament(double pReturnWinner, Random rand) {
            this.pReturnWinner = pReturnWinner;
            this.rand = rand;
        }

        @Override
        public T select(Collection<T> pop, BiFunction<T,T,Boolean> compareFun) {

            T i1 = F.randomElement(pop, rand);
            T i2 = F.randomElement(pop, rand);

            boolean i1wins = compareFun.apply(i1,i2);

            if (i1wins) {
                i1.addWin();
                i2.addLoss();
            } else {
                i2.addWin();
                i1.addLoss();
            }

            if (rand.nextDouble() < pReturnWinner) {
                return i1wins ? i1 : i2;
            } else {
                return i1wins ? i2 : i1;
            }
        }
    }



}
