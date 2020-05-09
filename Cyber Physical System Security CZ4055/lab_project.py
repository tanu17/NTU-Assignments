import numpy as np
# import pandas as pd
import csv

def pearson_r(x, y):
   """
   Compute Pearson correlation coefficient between two arrays by correlation matrix 2x2. 
   input x: list of dimension n
   input y: list of dimension y
   output corr_value: index = [0,1]
   """
   try:
      corr_mat = np.corrcoef(x, y)
      corr_value = abs(corr_mat[0,1])
   except:
      for i in range(len(y)):
         y[i] = float(y[i])
      corr_mat = np.corrcoef(x, y)
      corr_value = abs(corr_mat[0,1])
   return corr_value

def hamming_weight(x):
# calculate hamming weight of a string
   try:
      return bin(x).count("1")
   except:
      return bin(int(x,16)).count("1")


def weight_value(PT_2, key_2):
# gives hamming weight value of PT byte (plaintext byte, string length=2) and KByte  sbox operation result
   """
   Computes hamming weight which is proportional to bits switched in precharched bus
   input PT_2: byte of plaintext
   input key_2: byte of key to be guessed
   output hamming_weight(y_inter): hamming weight of (SBox of (PT_2 XOR key_2))
   """
   global Sbox_arr, Sbox

   result_inter = hex( int(bin(int(PT_2)),2) ^ int(bin(int(key_2,16)),2) )
   if len(result_inter)==3:
      y_inter = Sbox_arr[0][int(result_inter[2], 16)]
   else:
      y_inter = Sbox_arr[int(result_inter[2], 16)][int(result_inter[3], 16)]
   return hamming_weight(y_inter)

def all_PT(index):
   """
   input index: of plaintexts to be sifted through (will go from (0,len(plaintext,2)) 
   output all_pt_list: matrix of all combinations of key for all plaintext
      dimension= row- all key combination  ,  columns- all plaintexts
   """
   global plaintexts, key_brute_list

   all_pt_list = []
   for PT in plaintexts:
      pt_2 = PT[index:index+2]
      pt_2 = str(int(pt_2, 16))
      same_pt_all_keys = []
      for k in key_brute_list:
         same_pt_all_keys.append(weight_value(pt_2,k))
      all_pt_list.append(same_pt_all_keys)
   return (all_pt_list)

def find_key_byte(list_of_combination_for_key_byte):
   """
   Find the possible key using Pearson correlation of hamming weight and actual power values => can find the key and time from the attack
   input list_of_combination_for_key_byte: matrix of all possible key bytes combined with all plaintext byte by byte
   output: key byte that is most likely by pearson correlation
   """
   global power_values_transpose, key_brute_list

   matrix_corr_for_key2 = []
   list_of_combination_for_key_byte_transpose = list(map(list, zip(*list_of_combination_for_key_byte)))
   for row in list_of_combination_for_key_byte_transpose:
      temp_l = []
      for col in power_values_transpose:
         temp_l.append(pearson_r(row,col))
      matrix_corr_for_key2.append(temp_l)

   arr2D = np.array(matrix_corr_for_key2)
   max_index = np.where(arr2D == np.amax(arr2D))
   return(key_brute_list[int(max_index[0])])

def find_final_key():
   global plaintext_length
   Key = []
   for i in range(0,plaintext_length,2):
      Key.append(find_key_byte(all_PT(i)))
      print(Key)
   return (Key)

Sbox = [
      0x63, 0x7C, 0x77, 0x7B, 0xF2, 0x6B, 0x6F, 0xC5, 0x30, 0x01, 0x67, 0x2B, 0xFE, 0xD7, 0xAB, 0x76,
      0xCA, 0x82, 0xC9, 0x7D, 0xFA, 0x59, 0x47, 0xF0, 0xAD, 0xD4, 0xA2, 0xAF, 0x9C, 0xA4, 0x72, 0xC0,
      0xB7, 0xFD, 0x93, 0x26, 0x36, 0x3F, 0xF7, 0xCC, 0x34, 0xA5, 0xE5, 0xF1, 0x71, 0xD8, 0x31, 0x15,
      0x04, 0xC7, 0x23, 0xC3, 0x18, 0x96, 0x05, 0x9A, 0x07, 0x12, 0x80, 0xE2, 0xEB, 0x27, 0xB2, 0x75,
      0x09, 0x83, 0x2C, 0x1A, 0x1B, 0x6E, 0x5A, 0xA0, 0x52, 0x3B, 0xD6, 0xB3, 0x29, 0xE3, 0x2F, 0x84,
      0x53, 0xD1, 0x00, 0xED, 0x20, 0xFC, 0xB1, 0x5B, 0x6A, 0xCB, 0xBE, 0x39, 0x4A, 0x4C, 0x58, 0xCF,
      0xD0, 0xEF, 0xAA, 0xFB, 0x43, 0x4D, 0x33, 0x85, 0x45, 0xF9, 0x02, 0x7F, 0x50, 0x3C, 0x9F, 0xA8,
      0x51, 0xA3, 0x40, 0x8F, 0x92, 0x9D, 0x38, 0xF5, 0xBC, 0xB6, 0xDA, 0x21, 0x10, 0xFF, 0xF3, 0xD2,
      0xCD, 0x0C, 0x13, 0xEC, 0x5F, 0x97, 0x44, 0x17, 0xC4, 0xA7, 0x7E, 0x3D, 0x64, 0x5D, 0x19, 0x73,
      0x60, 0x81, 0x4F, 0xDC, 0x22, 0x2A, 0x90, 0x88, 0x46, 0xEE, 0xB8, 0x14, 0xDE, 0x5E, 0x0B, 0xDB,
      0xE0, 0x32, 0x3A, 0x0A, 0x49, 0x06, 0x24, 0x5C, 0xC2, 0xD3, 0xAC, 0x62, 0x91, 0x95, 0xE4, 0x79,
      0xE7, 0xC8, 0x37, 0x6D, 0x8D, 0xD5, 0x4E, 0xA9, 0x6C, 0x56, 0xF4, 0xEA, 0x65, 0x7A, 0xAE, 0x08,
      0xBA, 0x78, 0x25, 0x2E, 0x1C, 0xA6, 0xB4, 0xC6, 0xE8, 0xDD, 0x74, 0x1F, 0x4B, 0xBD, 0x8B, 0x8A,
      0x70, 0x3E, 0xB5, 0x66, 0x48, 0x03, 0xF6, 0x0E, 0x61, 0x35, 0x57, 0xB9, 0x86, 0xC1, 0x1D, 0x9E,
      0xE1, 0xF8, 0x98, 0x11, 0x69, 0xD9, 0x8E, 0x94, 0x9B, 0x1E, 0x87, 0xE9, 0xCE, 0x55, 0x28, 0xDF,
      0x8C, 0xA1, 0x89, 0x0D, 0xBF, 0xE6, 0x42, 0x68, 0x41, 0x99, 0x2D, 0x0F, 0xB0, 0x54, 0xBB, 0x16
      ]

#Sbox_arr = np.asarray(Sbox)

Sbox_arr = [Sbox[i:i+16] for i in range(0, len(Sbox), 16)]
for i in range(16):
   for j in range(16):
      Sbox_arr[i][j] = hex(Sbox_arr[i][j])

data = list(csv.reader(open("waveform.csv")))
len_time = len(data)
len_iter = len(data[0])

key_brute_list = []

for i in range (0,256):
   key_brute_list.append(hex(i))

# Y =  SBOX( PT XOR K_0 )

plaintexts = [a[0] for a in data]
plaintext_length = len(plaintexts[0])
ciphertexts= [a[1] for a in data]
power_values = [a[2:] for a in data]
power_values_transpose = list(map(list, zip(*power_values)))[:2500]

Key_of_data = find_final_key()
for index in range(len(Key_of_data)):
   Key_of_data[index] = Key_of_data[index].upper()
   print((Key_of_data[index])[2:])
   if len(Key_of_data[index])<2:
      Key_of_data[index] = "0" + Key_of_data[index]
print(Key_of_data)
