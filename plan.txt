HISTORY HACKDAY PROJECT, JAN 22-23rd 2011

TIME AND PLACE

Many wikipedia articles have geographic coordinates attached. Many deal with historic events. If we can find the subset of articles that have both we can create a dynamic visualization of Wikipedia's view of world history. With any luck we'll be able to see migrations, wars, the discovery of continents as the result of plotting many tiny data points. 

This idea is obviously inspired by the Facebook World Map (http://www.facebook.com/notes/facebook-engineering/visualizing-friendships/469716398919). However, ours would be animated, building up the picture gradually. For example, there probably won't be many articles in the
Americas until 1492, but after that, you should see the shape of America emerging as a collection of dots, each representing some historical event that someone thought was worthy of a Wikipedia article. 


GENERAL APPROACH
Step 1:
Go through XML dumps of Wikipedia articles. For each article, if it's a year (e.g. http://en.wikipedia.org/wiki/1982), extract the 'Events' section for later processing. If it's an article which references coordinates, process it to extract the coordinates and other desired metadata. 

So, we'll end up with two sets of data: 
* a list of events, each of which is a date and a sentence that references wikipedia articles. 
* a list of location-linked article data. 

Step 2: 
Go through all events. Look up the articles referenced by each. If they exist in our set of location-linked articles (and we may choose to apply other criteria to eliminate noise), we can imply a time-place link.

Step 3: 
* visualize the set of time-place links. 


VISUALIZATION IDEAS

* Dots should appear brightly and fade, but leave a mark to distinguish new from old events in the same space.


ANALYSIS IDEAS

* Visualization of national/geographic biases in the way Wikipedia represents history. There may be certain nations very sparsely represented. Some analysis of this could be interesting.

* We might be able to process data for foreign language versions of wikipedia too. Then it would be a simple matter to plot e.g. French versus German views of world history on the same map and see if biases emerge.

DATA MODEL

Each page referenced by a date will be represented something like the following:

{"latitude": 10.213, "longitude": 170.213, "year": 1983, "month": 11, "day": 21, "category": ["Countries", "Places"], "article_length": 123, "title": "United Kingdom"}

RENDERING THE VISUALIZATION

Rendering the visualization is a two stage process.  First you need to use the Java app (geo-vid-gen) to generate the separate png files that make up the frames of the video, then you need to use encoding software such as mencoder to stitch the images into a video file.

1) Make sure the config.properties file in src/main/resources are set properly with a inputLocation pointing to the json file with the events in it and the outputDirectory pointing to the required directory (this directory must exist). If the filenames are relative they must be relative to where you'll run the jar file from
2) Build the project:
  mvn assembly:assembly
3) Run the jar file (and wait a long time):
  java -jar target/geo-vid-gen-exe.jar
4) Ensure mencoder is installed:
  sudo apt-get install mencoder
5) cd to the chosen output directory and run something like:
  mencoder "mf://*.png" -mf w=1600:h=800:fps=25:type=png -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o output.mp4
Which will produce an output.mp4 file in the current directory.  The above is essentially "magic". If you want different results then you'll need to look around for direction on mencoder (or other video encoding tools).  It should be fairly obvious how to change the frame rate and video size above though.
  
  
