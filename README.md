Mathematical Language Processing
================================

# Run
* compile the maven project
* adapt the paths to your stratosphere environment in the file `cluster-run.sh`
* setup the right values for the parameters of the ranking algorithm also in `cluster-run.sh`
* execute the script


## Notice
To start the processor, an additional model file is needed. Download the Stanford POS tagger from http://nlp.stanford.edu/software/tagger.shtml. Within this archive is a directory called `pos-tagger-models/`, containing a variaty of model files for a couple of languages.

If uncertain, the `english-left3words-distsim.tagger` model is a good starting point.

Tested with http://nlp.stanford.edu/software/stanford-postagger-2012-11-11.zip ... the most recent version seems not to work.

## Installation
A installation guide for mediwiki-vagrant
```
sudo vi /etc/apt/sources.list.d/stratosphere.list
wget -q http://dev.stratosphere.eu/apt/stratosphere.gpg -O- | sudo apt-key add -
sudo apt-get update
sudo apt-get install stratosphere-dist
sudo service jobmanager start
sudo service taskmanager start
sudo jps
cd /usr/share/stratosphere-dist/log
/usr/share/stratosphere-dist/bin/stratosphere run -j  /usr/share/stratosphere-dist/examples/stratosphere-java-examples-0.5-SNAPSHOT-WordCount.jar -a 16 file:///var/log/syslog file:///home/vagrant/out -w
sudo apt-get install git openjdk-7-jdk maven
git clone https://github.com/physikerwelt/project-mlp
cd project-mlp
mvn clean package
```