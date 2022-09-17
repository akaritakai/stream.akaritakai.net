import sys
from datetime import date,datetime,time
if sys.version_info < (3, 7):
  from backports.datetime_fromisoformat import MonkeyPatch
  MonkeyPatch.patch_fromisoformat()
import argparse
import requests
import json
from durations import Duration
parser = argparse.ArgumentParser(description='foo twiddling')
parser.add_argument('--apiKey', metavar='KEY', help='apiKey')
parser.add_argument('--host', metavar='HOSTNAME', help='host name', default='localhost')
parser.add_argument('--port', type=int, metavar='N', help='port number', default=8080)
parser.add_argument('--delay', metavar='DURATION', type=lambda s: Duration(s))
parser.add_argument('--seekTime', metavar='DURATION', type=lambda s: Duration(s))
parser.add_argument('--startAt', metavar='INSTANT', type=lambda s: datetime.fromisoformat(s))

parser.add_argument('COMMAND', nargs='+')
def unknown(args):
  print("unknown command: ", " ".join(args.COMMAND))
def uri(args, path):
  return "http://%s:%d/%s"%(args.host,args.port,path)
class Stream():
  def __init__(self, args):
    self.args = args
  def start(self):
    if len(self.args.COMMAND) != 4:
      print("missing args")
      return
    args={'key':self.args.apiKey,'live':self.args.COMMAND[2] in ['true','yes','y','1','t'],'name':self.args.COMMAND[3]}
    if self.args.delay is not None:
      args['delay']=self.args.delay.to_miliseconds()
    if self.args.seekTime is not None:
      args['seekTime']=self.args.seekTime.to_miliseconds()
    if self.args.startAt is not None:
      args['startAt']=int(self.args.startAt.timestamp()*1000)
    print(requests.post(uri(self.args, 'stream/start'), data=json.dumps(args), headers={'content-type':'application/json'}).content)
  def stop(self):
    print(requests.post(uri(self.args, 'stream/stop'), data=json.dumps({'key':self.args.apiKey}), headers={'content-type':'application/json'}))
  def pause(self):
    print(requests.post(uri(self.args, 'stream/pause'), data=json.dumps({'key':self.args.apiKey}), headers={'content-type':'application/json'}))
  def resume(self):
    args={'key':self.args.apiKey}
    if self.args.delay is not None:
      args['delay']=self.args.delay.to_miliseconds()
    if self.args.seekTime is not None:
      args['seekTime']=self.args.seekTime.to_miliseconds()
    if self.args.startAt is not None:
      args['startAt']=int(self.args.startAt.timestamp()*1000)
    print(requests.post(uri(self.args, 'stream/resume'), data=json.dumps(args), headers={'content-type':'application/json'}))
class Chat():
  def __init__(self, args):
    self.args = args
  def clear(self):
    print(requests.post(uri(self.args, 'chat/clear'), data=json.dumps({'key':self.args.apiKey}), headers={'content-type':'application/json'}))
  def disable(self):
    print(requests.post(uri(self.args, 'chat/disable'), data=json.dumps({'key':self.args.apiKey}), headers={'content-type':'application/json'}))
  def enable(self):
    print(requests.post(uri(self.args, 'chat/enable'), data=json.dumps({'key':self.args.apiKey}), headers={'content-type':'application/json'}))
  def write(self):
    if len(self.args.COMMAND) < 4:
      print("missing nick and message")
      return
    nick = self.args.COMMAND[2]
    msg = " ".join(self.args.COMMAND[3:])
    print(requests.post(uri(self.args, 'chat/write'), data=json.dumps({'key':self.args.apiKey,'messageType':'TEXT','nickname':nick,'message':msg}), headers={'content-type':'application/json'}))
  def setemoji(self):
    if len(self.args.COMMAND) != 4:
      print("missing name and url")
      return
    nick = self.args.COMMAND[2]
    msg = self.args.COMMAND[3]
    print(requests.post(uri(self.args, 'chat/write'), data=json.dumps({'key':self.args.apiKey,'messageType':'EMOJI','nickname':nick,'message':msg}), headers={'content-type':'application/json'}))
class Main():
  def __init__(self, args):
    self.args = args
  def stream(self):
    if len(self.args.COMMAND) < 2:
      return unknown(self.args)
    stream = Stream(self.args)
    method = getattr(stream, self.args.COMMAND[1], lambda: unknown(self.args))
    return method()
  def chat(self):
    if len(self.args.COMMAND) < 2:
      return unknown(self.args)
    chat = Chat(self.args)
    method = getattr(chat, self.args.COMMAND[1], lambda: unknown(self.args))
    return method()
  def telemetry(self):
    print("telemetry")
  def health(self):
    print(requests.get(uri(self.args, 'health')).content.decode('utf-8'))
  def time(self):
    print(datetime.fromtimestamp(int(requests.get(uri(self.args, 'time')).content.decode('utf-8'))/1000.0))
if __name__ == "__main__":
  app = Main(parser.parse_args())
  method = getattr(app, app.args.COMMAND[0], lambda: unknown(app.args))
  method()

