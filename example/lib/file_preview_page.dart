import 'package:file_preview/file_preview.dart';
import 'package:file_preview_example/file_paths.dart';
import 'package:flutter/material.dart';

/// @Author: gstory
/// @CreateDate: 2021/12/27 10:27 上午
/// @Email gstory0404@gmail.com
/// @Description: dart类作用描述

class FilePreviewPage extends StatefulWidget {
  final String path;

  const FilePreviewPage({Key? key, required this.path}) : super(key: key);

  @override
  _FilePreviewPageState createState() => _FilePreviewPageState();
}

class _FilePreviewPageState extends State<FilePreviewPage> {
  late String fileExtension;
  FilePreviewController controller = FilePreviewController();

  @override
  void initState() {
    fileExtension = widget.path.split('.').last;
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('$fileExtension 预览'),
      ),
      body: Column(
        children: [
          Expanded(
            child: FilePreviewWidget(
              controller: controller,
              path: widget.path,
              callBack: FilePreviewCallBack(onShow: () {
                print("文件打开成功");
              }, onDownload: (progress) {
                print("文件下载进度$progress");
              }, onFail: (code, msg) {
                print("文件打开失败 $code  $msg");
              }),
            ),
          ),
          Container(
            margin: const EdgeInsets.only(bottom: 32),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              physics: const BouncingScrollPhysics(),
              child:
                  Row(children: files.map((e) => _changeFileBtn(e)).toList()),
            ),
          )
        ],
      ),
    );
  }

  Widget _changeFileBtn(String path) {
    String fileExtension = path.split('.').last;
    return TextButton(
      onPressed: () {
        controller.showFile(path);
        setState(() {
          this.fileExtension = fileExtension;
        });
      },
      child: Text(fileExtension),
    );
  }
}
