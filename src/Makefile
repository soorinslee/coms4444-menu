run:
	java -cp .:menu/org.json.jar menu.sim.Simulator --team g1 -m sharonConfig.dat -C 500 -p 4 -w 52 -s 2 -l log.txt -v -e meals.csv planners.csv pantries.csv satisfaction.csv

gui:
	java -cp .:menu/org.json.jar menu.sim.Simulator --team g5 -m a1.dat -C 500 -p 3 -w 52 -s 2 -l log.txt --gui -d -f 120

compile:
	javac -cp .:menu/org.json.jar menu/*/*.java

clean:
	rm menu/*/*.class
