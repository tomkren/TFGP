package cz.tomkren.fishtron.eva;


import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;

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

    class WithId extends Basic {

        private int indivId;

        public WithId(double val, boolean ok, int indivId) {
            super(val, ok);
            this.indivId = indivId;
        }

        public int getIndivId() {
            return indivId;
        }
    }

    /*class HaxFamilyInfo implements FitVal {

        private JSONObject familyInfo;

        public JSONObject getFamilyInfo() {return familyInfo;}


        public HaxFamilyInfo(JSONObject familyInfo) {
            this.familyInfo = familyInfo;
        }

        @Override public double getVal()   {throw new Error("HaxFamilyInfo.getVal is not supported.");}
        @Override public boolean isOK()    {throw new Error("HaxFamilyInfo.getVal is not supported.");}
    }*/

}
