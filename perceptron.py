# perceptron algorithm
from collections import Counter
from collections import defaultdict
import os
import itertools
import random
import operator

train_data = ''
vocab = []
train_data_list = []
validation_data_list = []
vocab_size = 0

def main():
  split_data()
  dictionary = count_word()
  vocab_size = build_vocab(dictionary)
  vector_set = create_vector_set(train_data_list, 4000,vocab_size)
  print 'vocab_size:', vocab_size
  weights = perceptron_train(vector_set, 4000, vocab_size)
  validate_set = create_vector_set(validation_data_list, 1000,vocab_size)
  perceptron_test(weights, validate_set, vocab_size)
  perceptron_test(weights, vector_set, vocab_size)
  
  avg_weight = average_perceptron(vector_set,4000,3, vocab_size)  
  perceptron_test(avg_weight, validate_set, vocab_size)
    

def split_data():
  with open(os.path.expanduser("~/Desktop/ps1_data/spam_train.txt")) as f:
    for line in itertools.islice(f, 0, 4000):
      #train_data = train_data +line
      train_data_list.append(line)
  with open(os.path.expanduser("~/Desktop/ps1_data/spam_train.txt")) as f:    
    for line in itertools.islice(f, 4000, None):
      validation_data_list.append(line)

#avoid counting words in the same email
def count_word():
  temp_word = ''
  d = defaultdict(int)
  for index in range(len(train_data_list)):
    currentEmail = train_data_list[index]
    for word in currentEmail.split():
      if word not in d:                     
        d[word]+=1
        temp_word = temp_word+ word + ' '
      else:
        if word not in temp_word.split():
          d[word]+=1
          temp_word = temp_word + word + ' '
    temp_word = ''
  return d
  print len(d)


#ignore words that appear in fewer than 30 emails, 0 and 1
def build_vocab(d):
  for word in d:
    if d[word] >= 30 and word != '1' and word != '0':           
      if word not in vocab:
        vocab.append(word)
  vocab_size = len(vocab)
  return vocab_size
  print 'number of words in vocabulary', vocab_size


#organize training set in this format[ (1,0,0,...1,0) , 1] first term is vector, second term is true label
def create_vector_set(train_data_list, email_num, vocab_size):
  matrix = [[0 for y in range(vocab_size)] for x in range(email_num)]
  for x in range(email_num):
    for y in range(vocab_size):
      if vocab[y] in train_data_list[x]:
        matrix[x][y] = 1
      else:
        matrix[x][y] = 0
  #print matrix
  training_set = [[0 for c in range(2)] for r in range(email_num)]    
  for r in range(email_num):
    training_set[r][0] = matrix[r]
    if int(train_data_list[r][0])== 0:
      training_set[r][1] = -1
    else:
      training_set[r][1] = int(train_data_list[r][0])        # retrive true label at the beginning of each email
  return training_set
  

def perceptron_train(set, email_num,vocab_size):
  iter, mistakes, calculated_label = 0, 0, 0
  weights = []  
  for i in range(vocab_size):                               #initialize to 0
    weights.append(0)
  #print weights  
  while True:
    temp_mistakes = 0
    for k in range(email_num):
      temp_vector = set[k][0]
      activation = 0
      for j in range(vocab_size):
        activation = activation + (weights[j]*temp_vector[j])
      if activation >= 0:
        calculated_label = 1
      else:
        calculated_label = -1
        
      if calculated_label != set[k][1]:
        temp_mistakes = temp_mistakes+1
        mistakes = mistakes+1
        for r in range(vocab_size):
          weights[r]= weights[r] + (set[k][1]*temp_vector[r])

    iter= iter+1
    #print weights
    print 'temp_mistakes', temp_mistakes
    if temp_mistakes == 0:
      break

  print 'iters:',iter,'mistakes:',mistakes
  return weights

def perceptron_test(weights, set, vocab_size):
  activation, calculated_label, mistakes = 0, 0, 0
  
  for j in range(len(set)):
    activation = 0
    temp_vector = set[j][0]
    for i in range(vocab_size):
      activation = activation + (weights[i]*temp_vector[i])
    if activation >= 0:
      calculated_label = 1
    else:
      calculated_label = -1
      
    if calculated_label != set[j][1]:
      mistakes= mistakes+1
  error = mistakes/float(len(set))
  print 'Error:', error, 'Mistakes:', mistakes, 'len(set):', len(set)

def average_perceptron(set, email_num, max_iter, vocab_size):
  iter, mistakes, activation, calculated_label = 0, 0, 0, 0
  weights = []
  avg_weight = []
  for i in range(vocab_size):                  #initialize to all 0s
    weights.append(0)
    avg_weight.append(0)

  for k in range(max_iter):
    for g in range(email_num):
      temp_vector = set[g][0]
      activation = 0
      for j in range(vocab_size):
        activation = activation + (weights[j]*temp_vector[j])
      if activation >= 0:
        calculated_label = 1
      else:
        calculated_label = -1
      if calculated_label != set[g][1]:
        mistakes = mistakes+1
        for r in range(vocab_size):
          weights[r]= weights[r]+(set[g][1]*temp_vector[r])
      for z in range(len(weights)):
        avg_weight[z]= avg_weight[z]+weights[z]
  for h in range(len(weights)):
    avg_weight[h]= avg_weight[h]/max_iter
  print 'iters:', iter,'mistakes:', mistakes
  return avg_weight

def norm(vector):
  norm = 0
  for i in range(len(vector)):
    norm = norm+ vector[i]**2
  norm = norm**0.5
  return norm
  
  
def pegasos_svm_train(data, lamda, vocab_size):
  iter, mistakes, t = 0, 0, 0
  vector_length = 0
  weights = []
  u = []

  for l in range(vocab_size):                               #initialize to 0
    weights.append(0)
    u.append(0)

  for i in range(20):
    for j in range(len(data)):
      t = t+1
      eta = 1/(t*lamda)
      temp_vector = data[j][0]        #  j-th email
      dot_prod = 0
      dot_prod = dot_product(weights, temp_vector)
      if data[j][1]*dot_prod < 1:                          #line5
        for k in range(vocab_size):
          weights[k]= (1 - 1/t)*weights[k]
          u[k]= weights[k]+(eta*temp_vector[k]*data[j][1])
      else:                                                   #line7
        for s in range(vocab_size):
          u[s]= (1 - 1/t)*weights[s]
      
      vector_length = norm(u)
      N = (1/(lamda**0.5))/vector_length
      if N < 1:
        for y in range(vocab_size):
          weights[y] = N * u[y]
      else:
        for x in range(vocab_size):
          weights[x] = u[x]
    print 'iter:', i+1
    svm_obj = 0
    for h in range(len(data)):
      temp_num = 1 - data[h][1]*dot_product(weights, data[h][0])
      if temp_num > 1:
        svm_obj = svm_obj+temp_num
      else:
        svm_obj = svm_obj
    svm_obj = svm_obj/len(data)+(lamda/2)*norm(weights)**2
    print 'svm_obj', svm_obj
          
  return weights  
  

def find_svm(data, w, vocab_size):
  prod = 0
  length = norm(w)
  r = 1/length
  print r
  for j in range(len(data)):
    temp_vector = data[j][0]
    for k in range(vocab_size):
      prod = prod + (w[k]*temp_vector[k])
    if prod <= 1.01 and prod >= -1.01:
      print 'SVM:', j+1 

def dot_product(vector1, vector2):
  return sum(map( operator.mul, vector1, vector2))


    
           

main()