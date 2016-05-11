package cz.tomkren.fishtron.workflows;

import cz.tomkren.fishtron.terms.SmartLibrary;
import org.json.JSONObject;

/** Created by tom on 7.11.2015.*/

// TODO DRY!!! dvakrát vyjmenovaný ty stringy, vopravit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!!!!!!!!!!!!!!!!! !!! !! ! !! !!!

public class DataScientistLibs {

    public static final SmartLibrary DATA_SCIENTIST_WITH_PARAMS_01 = mkDataScientistLib01FromParamsInfo("{\"copy\": {}, \"kMeans\": {}, \"DT\": {\"criterion\": [\"gini\", \"entropy\"], \"max_features\": [0.05, 0.1, 0.25, 0.5, 0.75, 1], \"min_samples_split\": [1, 2, 5, 10, 20], \"min_samples_leaf\": [1, 2, 5, 10, 20], \"max_depth\": [1, 2, 5, 10, 15, 25, 50, 100]}, \"union\": {}, \"vote\": {}, \"gaussianNB\": {}, \"PCA\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1], \"whiten\": [false, true]}, \"logR\": {\"tol\": [0.0001, 0.001, 0.01], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15], \"penalty\": [\"l1\", \"l2\"]}, \"kBest\": {\"feat_frac\": [0.01, 0.05, 0.1, 0.25, 0.5, 0.75, 1]}, \"SVC\": {\"tol\": [0.0001, 0.001, 0.01], \"gamma\": [0.0, 0.0001, 0.001, 0.01, 0.1, 0.5], \"C\": [0.1, 0.5, 1.0, 2, 5, 10, 15]}}");


    public static SmartLibrary mkDataScientistLib01FromParamsInfo(String paramsInfo) {
        return mkDataScientistLib01FromParamsInfo(new JSONObject(paramsInfo));
    }

    public static SmartLibrary mkDataScientistLib01FromParamsInfo(JSONObject paramsInfo) {
        return SmartLibrary.mk("cz.tomkren.fishtron.workflows.",paramsInfo,
                "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                "TypedDag.dia0( TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
                "TypedDag.split( TypedDag: D => (V D n) , MyList: V (D => LD) n ) : D => (V LD n)",
                "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
                "MyList.nil : V a 0",
                "PCA : D => D",
                "kBest : D => D",
                "kMeans : D => (V D (S(S n)))",
                "copy : D => (V D (S(S n)))",
                "SVC        : D => LD",
                "logR       : D => LD",
                "gaussianNB : D => LD",
                "DT         : D => LD",
                "vote : (V LD (S(S n))) => LD"
        );
    }

    public static final SmartLibrary DATA_SCIENTIST_01 = SmartLibrary.mk("cz.tomkren.fishtron.workflows.",
            "TypedDag.dia( TypedDag: D => D , TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
            "TypedDag.dia0( TypedDag: D => (V LD n) , TypedDag: (V LD n) => LD ) : D => LD",
            "TypedDag.split( TypedDag: D => (V D n) , MyList: V (D => LD) n ) : D => (V LD n)",
            "MyList.cons( Object: a , MyList: V a n ) : V a (S n)",
            "MyList.nil : V a 0",
            "PCA : D => D",
            "kBest : D => D",
            "kMeans : D => (V D (S(S n)))",
            "copy : D => (V D (S(S n)))",
            "SVC        : D => LD",
            "logR       : D => LD",
            "gaussianNB : D => LD",
            "DT         : D => LD",
            "vote : (V LD (S(S n))) => LD"
    );

}
