function mkBadTreeExample() {
    return {
        "node": "@",
        "arg": {
            "node": "k",
            "type": "((P A (P A A)) -> (x2 -> (P A (P A A))))"
        },
        "type": "((P A (P A A)) -> (P A (P A A)))",
        "fun": {
            "node": "@",
            "arg": {
                "node": "@",
                "arg": {
                    "node": "@",
                    "arg": {
                        "node": "k",
                        "type": "((P A (P A A)) -> ((x2 -> (P A (P A A))) -> (P A (P A A))))"
                    },
                    "type": "(((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A))))",
                    "fun": {
                        "node": "s",
                        "type": "(((P A (P A A)) -> ((x2 -> (P A (P A A))) -> (P A (P A A)))) -> (((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A)))))"
                    }
                },
                "type": "(P ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A)))) (((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A)))))",
                "fun": {
                    "node": "@",
                    "arg": {
                        "node": "fst",
                        "type": "((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A))))"
                    },
                    "type": "((((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A)))) -> (P ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A)))) (((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A))))))",
                    "fun": {
                        "node": "mkP",
                        "type": "(((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A)))) -> ((((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A)))) -> (P ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A)))) (((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A)))))))"
                    }
                }
            },
            "type": "(((P A (P A A)) -> (x2 -> (P A (P A A)))) -> ((P A (P A A)) -> (P A (P A A))))",
            "error": true,
            "fun": {
                "node": "snd",
                "type": "((P ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A)))) (((P A (P A A)) -> ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A))))) -> ((P A (P A A)) -> (P A (P A A))))) -> (((P A (P A A)) -> ((P (x2 -> (P A (P A A))) x2) -> (x2 -> (P A (P A A))))) -> ((P A (P A A)) -> (P A (P A A)))))"
            }
        }
    };
}