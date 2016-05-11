package cz.tomkren.fishtron.eva;

public interface FitVal {
    double getVal();
    boolean isOK();

    class Basic implements FitVal {
        private double val;
        private boolean ok;

        public Basic(double val) {
            this(val, false);
        }

        public Basic(double val, boolean ok) {
            this.val = val;
            this.ok = ok;
        }

        @Override public double getVal() {return val;}
        @Override public boolean isOK() {return ok;}

        @Override
        public String toString() {return (ok?"[OK] ":"")+val;}
    }

}
