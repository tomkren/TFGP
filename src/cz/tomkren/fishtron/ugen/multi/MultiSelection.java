package cz.tomkren.fishtron.ugen.multi;

import net.fishtron.utils.F;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/** Created by tom on 07.03.2017.*/

public interface MultiSelection<Indiv extends MultiIndiv> {

    Indiv select(Collection<Indiv> pop, List<Boolean> isMaxis);

    class Tournament<T extends MultiIndiv> implements MultiSelection<T> {

        private final double pReturnWinner;
        private final Random rand;

        public Tournament(double pReturnWinner, Random rand) {
            this.pReturnWinner = pReturnWinner;
            this.rand = rand;
        }

        @Override
        public T select(Collection<T> pop, List<Boolean> isMaxis) {

            T i1 = F.randomElement(pop, rand);
            T i2 = F.randomElement(pop, rand);

            int comparison = MultiIndiv.compare(i1, i2, isMaxis);
            boolean i1wins = comparison < 0;

            if (rand.nextDouble() < pReturnWinner) {
                return i1wins ? i1 : i2;
            } else {
                return i1wins ? i2 : i1;
            }
        }
    }

}
