# TODO not yet tested properly


# Creation of python3 venv for the GPML (in particular for the dag-evaluate part)
python3 -m venv gpml-env

# Activate the venv
source gpml-env/bin/activate


# clone dag-evaluate (TODO is done elswere, but..)

git clone https://github.com/martinpilat/dag-evaluate.git

cd dag-evaluate

# TODO byly tam podezřelý symboly když se dá cat nebo nano na requirements.txt TODO !!!!! old requirements !!! see email od martina
pip install -r requirements.txt

mkdir mylogs

python xmlrpc_interface.py mylogs 3 configs/wine-1.json

