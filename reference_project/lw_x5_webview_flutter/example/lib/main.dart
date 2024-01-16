import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:x5_webview/x5_sdk.dart';

void main() {
  X5Sdk.setDownloadWithoutWifi(true); //没有x5内核，是否在非wifi模式下载内核。默认false
  X5Sdk.init().then((isOK) {
    print(isOK ? "X5内核成功加载" : "X5内核加载失败");
  });
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(home: HomePage());
  }
}

class HomePage extends StatefulWidget {
  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String docPath = '/sdcard/Download/demo.doc';
  String docxPath = '/sdcard/Download/demo.docx';
  String pptPath = '/sdcard/Download/demo.ppt';
  String pptxPath = '/sdcard/Download/demo.pptx';
  String xlsPath = '/sdcard/Download/demo.xls';
  String xlsxPath = '/sdcard/Download/demo.xlsx';
  // String localPath = '/storage/emulated/0/Android/data/io.morbit.morbit_flutter/files/logs.csv'; //android x5文件sdk需要正确格式 本地文件才能正确打开

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('X5打开本地文件示例'),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: <Widget>[
          Center(
            child: RaisedButton(
              child: Text('打开doc'),
              onPressed: () {
                X5Sdk.openFile(docPath);
              },
            ),
          ),
          Center(
            child: RaisedButton(
              child: Text('打开docx'),
              onPressed: () {
                X5Sdk.openFile(docxPath);
              },
            ),
          ),
          Center(
            child: RaisedButton(
              child: Text('打开ppt'),
              onPressed: () {
                X5Sdk.openFile(pptPath);
              },
            ),
          ),
          Center(
            child: RaisedButton(
              child: Text('打开pptx'),
              onPressed: () {
                X5Sdk.openFile(pptxPath);
              },
            ),
          ),
          Center(
            child: RaisedButton(
              child: Text('打开xls'),
              onPressed: () {
                X5Sdk.openFile(xlsPath);
              },
            ),
          ),
          Center(
            child: RaisedButton(
              child: Text('打开xlsx'),
              onPressed: () {
                X5Sdk.openFile(xlsxPath);
              },
            ),
          ),
        ],
      ),
    );
  }
}
