import 'package:file_preview/file_preview.dart';
import 'package:file_preview_example/file_paths.dart';
import 'package:file_preview_example/file_preview_page.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final status = await [
    Permission.storage,
    Permission.manageExternalStorage,
  ].request();
  print(status);
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  bool isInit = false;
  String? version;

  @override
  void initState() {
    _initTBS();
    _tbsVersion();
    super.initState();
  }

  Future<void> _initTBS() async {
    isInit = await FilePreview.initTBS();
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> _tbsVersion() async {
    version = await FilePreview.tbsVersion();
    isInit = await FilePreview.tbsHasInit();
    if (mounted) {
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('File Preview'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text('TBS初始化 $isInit'),
            Text('TBS版本号 $version'),
            MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: const Text('初始化TBS'),
              onPressed: () async {
                _initTBS();
              },
            ),
            MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: const Text('检测TBS是否初始化成功'),
              onPressed: () async {
                isInit = await FilePreview.tbsHasInit();
                setState(() {});
              },
            ),
            ...files.map((e) => openFileBtn(e)),
            MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: const Text('清理本地缓存文件（android有效）'),
              onPressed: () async {
                var delete = await FilePreview.deleteCache();
                if (delete) {
                  print("缓存文件清理成功");
                }
              },
            ),
            MaterialButton(
              color: Colors.blue,
              textColor: Colors.white,
              child: const Text('TBS调试页面'),
              onPressed: () async {
                await FilePreview.tbsDebug();
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget openFileBtn(String path) {
    String fileExtension = path.split('.').last;
    String title = '$fileExtension 预览';
    return MaterialButton(
      color: Colors.blue,
      textColor: Colors.white,
      child: Text(title),
      onPressed: () async {
        isInit = await FilePreview.tbsHasInit();
        setState(() {});
        if (!isInit) {
          _initTBS();
          return;
        }

        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (_) {
              return FilePreviewPage(path: path);
            },
          ),
        );
      },
    );
  }
}
