package net.fishtron.eva.simple;

import net.fishtron.utils.AB;
import net.fishtron.utils.Distribution;
import net.fishtron.utils.Weighted;

import java.util.List;
import java.util.Random;

/** Created by tom on 2.7.2015.*/

public interface Selection<T extends Weighted> {

    T select(Distribution<T> dist);


    class Roulette<T extends Weighted> implements Selection<T> {

        private final Random rand;
        public Roulette(Random rand) {this.rand = rand;}

        @Override
        public T select(Distribution<T> dist) {
            return dist.get(rand);
        }
    }

    class Tournament<T extends Weighted> implements Selection<T> {

        private final double pReturnWinner;
        private final Random rand;

        public Tournament(double pReturnWinner, Random rand) {
            this.pReturnWinner = pReturnWinner;
            this.rand = rand;
        }

        @Override
        public T select(Distribution<T> dist) {
            return dist.tournamentGet(pReturnWinner, rand);
        }
    }

    class Tournament2<T extends Weighted> implements Selection<T> {

        private final int size;
        private final Random rand;

        public Tournament2(int size, Random rand) {
            this.size = size;
            this.rand = rand;
        }

        @Override
        public T select(Distribution<T> dist) {
            return dist.tournamentGet2(size, rand);
        }
    }

    interface TournamentObserver<T extends Weighted> {
        void tellTournamentResult(List<T> candidates, T winner);
    }

    class ObservableTournament2<T extends Weighted> implements Selection<T> {

        private final int size;
        private final Random rand;
        private final TournamentObserver<T> observer;

        public ObservableTournament2(int size, Random rand, TournamentObserver<T> observer) {
            this.size = size;
            this.rand = rand;
            this.observer = observer;
        }

        @Override
        public T select(Distribution<T> dist) {
            AB<T,List<T>> result = dist.tournamentGet2_withCandidates(size, rand);

            observer.tellTournamentResult(result._2(),result._1());

            return result._1();
        }
    }
}
