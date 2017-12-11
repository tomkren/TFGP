
mkdir sources
cd sources


@IF EXIST "TFGP" (
	echo No need to clone 'TFGP'.
	cd TFGP
	git pull
	cd ..
) ELSE (
    echo Start clone 'TFGP' ...
	git clone https://github.com/tomkren/TFGP.git
)


@IF EXIST "dag-evaluate" (
	echo No need to clone 'dag-evaluate'.
	cd dag-evaluate
	git pull
	cd ..
) ELSE (
    echo Start clone 'dag-evaluate' ...
	git clone https://github.com/martinpilat/dag-evaluate
)

cd ..
