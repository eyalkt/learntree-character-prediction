# learntree-character-prediction
Data Analysis CS project, deploying Learning-Tree algorithm to determine a character from an MNIST-like photo representation.

Input format for learning part and prediction part is CVS data-set of 28x28 pixels, preceeded with the actual char (tag).<br>
each pixel is a value between 0 and 255, which represent its gray level (0 being white and 255 black). <br>
see http://yann.lecun.com/exdb/mnist/ for more information about the dataset.

To run the project one can call 'make' (in Linux shell), which will create the executables 'learntree' and 'predict'.
more information about the implementation and 'conditions' (mentioned below) is in Project-Report.txt

<p>
<strong>The learning algorithm:</strong><br>
The command line to run the learning algorithm is as folows:

<code>learntree <1/2> &lt;P&gt; &lt;L&gt; <trainingset_filename> <outputtree_filename></code>

The input parameters are: 
<ul>
<li> 1/2 - which version of the learning algorithm to run. <br>
  1 - the first version with basic conditions, <br>
  2 - the second version, with better more precise conditions.
</li><li> P - the percent of the training set that should be used for validation 
</li><li> L - the maximal power of 2 for tree sizes to examine.
</li><li> trainingset_filename - the name/path of the training set file. The format of this file is the
  CSV format mentioned above. Each line represents a single example. The file can have any 
  number of lines depending on the size of the input training set. The examples in the file can 
  be from the MNIST dataset but can also be others (in the same format). 
</li><li> outputtree_filename - the name/path of the file into which the algorithm will output the 
  decision tree that it learned based on the training set. The format of the file is Java Serialized. 
</li>
</ul>

The output of "learntree" is the tree file. In addition, the following information is printed: <br>
<blockquote>
num: &lt;number of training examples&gt;<br>
error: &lt;error of the learned tree on the training set (in whole percents, between 0 and 100, no % sign)&gt;<br>
size: &lt;size of the learned tree&gt;<br>
</blockquote>
</p>

<p>
<strong>The prediction algorithm:</strong><br>
The command line to run the prediction algorithm is as folows:

<code>predict &lt;tree_filename&gt; &lt;testset_filename&gt;</code>

The input parameters are:
<ul>
  <li>tree_filename - the name/path of the file that describes the tree to use for prediction. This 
      will be a file that was output by running you "learntree", with the first version or the second 
      version of the algorithm.
</li><li>testset_filename - the name/path of the file that includes the test examples which you 
      should predict, in the CSV format mentioned above. Note that the format also includes a 
      label for each example, the algorithm ignores it.
  </li>
  </ul>

The output of "predict" is a list of labels which is printed to the standard output.<br>
For instance, if the test file has 3 examples, the output of "predict" can be:<br>
<blockquote>
2<br>
a<br>
1<br>
</blockquote>
Meaning that it predicts that the label of the first test example is '2', the second test example is 'a' and the third one is '1'.
</p>
