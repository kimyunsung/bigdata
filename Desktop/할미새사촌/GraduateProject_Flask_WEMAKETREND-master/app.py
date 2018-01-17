#-*- coding:utf-8 -*-
import json
from elasticsearch import Elasticsearch
from flask import Flask, render_template,redirect, url_for,request
import sys
from lxml import etree
import urllib2
reload(sys)
sys.setdefaultencoding('utf-8')
app = Flask(__name__)
es = Elasticsearch()

@app.route("/")
def main_page():
    return render_template('index.html')

@app.route("/WtoF")
def WtoF():
    response = urllib2.urlopen(
        'https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&oquery=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C%EB%82%A0%EC%94%A8&ie=utf8&query=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C%EB%82%A0%EC%94%A8')
    html = response.read()
    tree = etree.HTML(html)
    temperture = tree.xpath('//div[@class=\'fl\']/em/text()')[0]
    specification = tree.xpath('//div[@class=\'fl\']/em/strong/text()')[0]
    dust = tree.xpath('//p/a[@class=\'_fine_dust_exp_open\']/span/text()')[0]
    date = tree.xpath('//h4/span/text()')[0]
    factor = []
    if (int(temperture) < 6):
        factor.append("temperture_low")
    elif (int(temperture) > 27):
        factor.append("temperture_high")
    if '비' in specification.encode('utf-8'):
        factor.append("rain")
    elif '눈' in specification.encode('utf-8'):
        factor.append("snow")
    if '흐림 ' in specification.encode('utf-8'):
        factor.append("fog")
    if '나쁨 ' in dust.encode('utf-8'):
        factor.append("yellow_dust")
    if (int(date.split('.')[1]) == 4 or int(date.split('.')[1]) == 11 or int(date.split('.')[1]) == 18 or int(
            date.split('.')[1]) == 25):
        factor.append("friday")
    if (int(date.split('.')[1]) == 5 or int(date.split('.')[1]) == 6 or int(date.split('.')[1]) == 12 or int(
            date.split('.')[1]) == 13 or int(date.split('.')[1]) == 19 or int(date.split('.')[1]) == 20 or int(
            date.split('.')[1]) == 26 or int(date.split('.')[1]) == 27):
        factor.append("weekend")
    res = []
    size = len(factor)
    if (size == 1):
        search_results = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},
                                                                             "query": {"bool": {"must": [{"match": {
                                                                                 "factor": factor[0]}}, {
                                                                                                             "match": {
                                                                                                                 "positive": "true"}}],
                                                                                                "filter": [{"range": {
                                                                                                    "1-p": {
                                                                                                        "gte": 0.99}}}]}}},
                                   size=800)
        results_1 = search_results['hits']['hits']
        exist = []
        max_1 = 0
        temp = []
        for i in range(0, len(results_1)):
            if (results_1[i]['_source']['food'] in exist):  # 겹치면 삭제
                continue
            else:
                exist.append(results_1[i]['_source']['food'])
                temp.append(results_1[i])
            max_1 = max(max_1, results_1[i]['_source']['F'])
        results_1 = temp
        for result in results_1:
            result['_source']['F'] = (result['_source']['F'] * 100) / max_1
            res.append(result['_source'])

    elif (size > 1):
        search_results_1 = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},
                                                                               "query": {"bool": {"must": [{"match": {
                                                                                   "factor": factor[0]}}, {
                                                                                                               "match": {
                                                                                                                   "positive": "true"}}]
                                                                                                
                                                                                             
                                                                                                         }}},
                                     size=800)
        #search_results_2 = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},
         #                                                                      "query": {"bool": {"must": [{"match": {
          #                                                                         "factor": factor[1]}}, {
           #                                                                                                    "match": {
            #                                                                                                       "positive": "true"}}],
             #                                                                                     "filter": [{"range": {
              #                                                                                        "1-p": {
               #                                                                                           "gte": 0.99}}}]}}},
                #                     size=800)
        search_results_2 = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},
                                                                               "query": {"bool": {"must": [{"match": {
                                                                                   "factor": factor[1]}}, {
                                                                                                               "match": {
                                                                                                                   "positive": "true"}}]
                                                                                                      
                                                                                                          }}},
                                     size=800)

        results_1 = search_results_1['hits']['hits']
        results_2 = search_results_2['hits']['hits']

        exist = []
        max_1 = 0
        temp = []
        for i in range(0, len(results_1)):
            if (results_1[i]['_source']['food'] in exist):  # 겹치면 삭제
                continue
            else:
                exist.append(results_1[i]['_source']['food'])
                temp.append(results_1[i])
            max_1 = max(max_1, results_1[i]['_source']['F'])
        results_1 = temp

        exist = []
        max_2 = 0
        temp = []
        for i in range(0, len(results_2)):
            if (results_2[i]['_source']['food'] in exist):  # 겹치면 삭제
                continue
            else:
                exist.append(results_2[i]['_source']['food'])
                temp.append(results_2[i])
            max_2 = max(max_2, results_2[i]['_source']['F'])
        results_2 = temp


        for result in results_1:
            result['_source']['F'] = (result['_source']['F'] * 100) / max_1
        for result in results_2:
            result['_source']['F'] = (result['_source']['F'] * 100) / max_2

        k = 0
        res = []
        for i in range(0, len(results_1)):
            for j in range(0, len(results_2)):
                if (results_1[i]['_source']['food'] == results_2[j]['_source']['food']):
                    results_1[i]['_source']['F'] = results_1[i]['_source']['F'] + results_2[j]['_source']['F']
                    res.append(results_1[i]['_source'])

        # 하나 속성만 가질 때는 안보여주는 것으로 하지
        res = sorted(res, key=lambda k: k['F'], reverse=True)
    return render_template('WtoF.html',res=res,fac = factor)

@app.route("/FtoW", methods = ['POST', 'GET'])
def FtoW():
    res =[]
    if request.method == 'POST':
        if(request.form):
            search_results = es.search(index="category", doc_type='food',
                                       body={"query":{"match":{"food": request.form.get('search_text')}}},
                                       size=9)
            results_1 = search_results['hits']['hits']
            for result in results_1:
                res.append(result['_source'])
    return render_template('FtoW.html', res=res)

@app.route("/chart/<string:data>")
def chartmaking(data):
    #recommend_dic={"thunder_lightning":"천둥치는 날","halo":"안개 조금있는날","temperture_high":"더운 날", "temperture_low":"추운 날", "snow":"눈오는 날", "yellow_dust":"황사 주의", "fog":"안개 많은날", "rain":"비오는 날"}
    recommend_dic={0:"천둥치는 날",1:"안개 조금있는날",2:"더운 날", 3:"추운 날", 4:"눈오는 날", 5:"황사 주의", 6:"안개 많은날", 7:"비오는 날"}
    weather_dic = {"thunder_lightning":0,"halo":1,"temperture_high":2, "temperture_low":3, "snow":4, "yellow_dust":5, "fog":6, "rain":7}
    str=data
    search_results1 = es.search(index="analysis", doc_type='ANOVA', body={"query":{"bool":  {"must": {"match": {"food": str}}}}}, size=100)
    results1 = search_results1['hits']['hits']
    max_1 = 0

    search_results2 = es.search(index="instagram", doc_type='numOfFoodPostsPerDay',
                               body={"sort": {"date": {"order": "asc"}}, "query": {"bool":  {"must":{"match": {"food": str}}}}},
                               size=1000)

    results2 = search_results2['hits']['hits']
    max_1 = 0
    daylist = []
    countlist = []
    likeslist = []
    for i, result in enumerate(results2):
        if i == 1000:
            break
        likes = max(max_1, result['_source']['likes'])
        count = result['_source']['count']
        day = result['_source']['date']
        daylist.append(int(day))
        countlist.append(count)
        likeslist.append(likes)

    data4 = [0,0,0,0,0,0,0,0]
    data5=  [0,0,0,0,0,0,0,0]
    for i, result in enumerate(results1):

        ax_1 = max(max_1, result['_source']['F'])
        positive=result['_source']['positive']
        factor = result['_source']['factor']
        ax_2=ax_1
        try:
            index=weather_dic[factor.strip()]
            value_recommend = recommend_dic[index]
            if positive.strip()=='false':
                ax_2 = (-1) * ax_1
                ax_1=0
            data4[index]=ax_1
            data5[index]=ax_2

        except:
            continue
    recommend_list=[]
    no_recommend_list=[]
    newdic_recommend=[]
    newdic_no_recommend=[]
    for index,value in enumerate(data5):
        if value>4:
            newdic_recommend.append(value)

        elif value<0:
            newdic_no_recommend.append(value)
    #newdic_recommend.sort(reverse=True)

    newdic_recommend2 = sorted(newdic_recommend, reverse=True)

    newdic_no_recommend2 = sorted(newdic_no_recommend, reverse=False)
    #newdic_no_recommend.sort(reverse=False)
    for element in newdic_recommend2:
        recommend_list.append((recommend_dic[data5.index(element)]))
    for element in newdic_no_recommend2:
        no_recommend_list.append(recommend_dic[data5.index(element)])


    return render_template('chart.html',name=data,data1=daylist,data2=countlist,data3=likeslist,data4=data4,data5=data4,recommend=recommend_list,no_recommend=no_recommend_list)

@app.route('/result',methods = ['POST', 'GET'])
def routind():
    res = []
    if request.method == 'POST':
        if(request.form):
            size = len(request.form)
            if (size == 1):
                search_results = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},"query":{"bool" : { "must" : [{"match":{"factor": request.form.keys()[0]}},{"match":{"positive" : "true"}}],"filter" : [{"range":{"1-p":{"gte" : 0.99}}}]}}}, size=800)
                results_1 = search_results['hits']['hits']
                exist = []
                max_1 = 0
                temp = []
                for i in range(0, len(results_1)):
                    if (results_1[i]['_source']['food'] in exist):  # 겹치면 삭제
                        continue
                    else:
                        exist.append(results_1[i]['_source']['food'])
                        temp.append(results_1[i])
                    max_1 = max(max_1, results_1[i]['_source']['F'])
                results_1 = temp
                for result in results_1:
                    result['_source']['F'] = (result['_source']['F'] * 100) / max_1
                    res.append(result['_source'])

            elif (size == 2):
                search_results_1 = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},"query":{"bool" : { "must" : [{"match":{"factor": request.form.keys()[0]}},{"match":{"positive" : "true"}}]}}}, size=800)
                search_results_2 = es.search(index="analysis", doc_type='ANOVA', body={"sort": {"F": {"order": "desc"}},"query":{"bool" : { "must" : [{"match":{"factor": request.form.keys()[1]}},{"match":{"positive" : "true"}}]}}}, size=800)
                results_1 = search_results_1['hits']['hits']
                results_2 = search_results_2['hits']['hits']

                exist = []
                max_1 = 0
                temp = []
                for i in range(0, len(results_1)):
                    if(results_1[i]['_source']['food'] in exist): #겹치면 삭제
                        continue
                    else:
                        exist.append(results_1[i]['_source']['food'])
                        temp.append(results_1[i])
                    max_1 = max(max_1,results_1[i]['_source']['F'])
                results_1 = temp

                exist = []
                max_2 = 0
                temp = []
                for i in range(0, len(results_2)):
                    if(results_2[i]['_source']['food'] in exist):  # 겹치면 삭제
                        continue
                    else:
                        exist.append(results_2[i]['_source']['food'])
                        temp.append(results_2[i])
                    max_2 = max(max_2, results_2[i]['_source']['F'])
                results_2 = temp



                for result in results_1:
                    result['_source']['F'] = (result['_source']['F']*100)/max_1
                for result in results_2:
                    result['_source']['F'] = (result['_source']['F'] * 100)/max_2

                k=0
                res = []
                for i in range(0,len(results_1)):
                    for j in range(0, len(results_2)):
                        if(results_1[i]['_source']['food'] == results_2[j]['_source']['food']):
                            results_1[i]['_source']['F'] = results_1[i]['_source']['F'] + results_2[j]['_source']['F']
                            res.append(results_1[i]['_source'])

                #하나 속성만 가질 때는 안보여주는 것으로 하지
                res = sorted(res, key=lambda k: k['F'],reverse=True)
    return render_template('WtoF.html', res=res, fac = request.form.keys())

if __name__ == "__main__":
    app.debug = True
    app.run(host = "0.0.0.0")

