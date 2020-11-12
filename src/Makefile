run:
	java -cp .:menu/org.json.jar menu.sim.Simulator --team g2 -m christmas.dat -C 2000 -p 30 -w 52 -s 42 -l log.txt -v -e meals.csv planners.csv pantries.csv satisfaction.csv

gui:
	java -cp .:menu/org.json.jar menu.sim.Simulator --team g2 -m family.dat -C 200 -p 3 -w 52 -s 42 -l log.txt --gui -c -f 60

compile:
	javac -cp .:menu/org.json.jar menu/*/*.java

clean:
	rm menu/*/*.class
