# chatSubTranslator
a simple English to Chinese subtitle translator base on chatGPT,only support SRT by far.Also,audio to SRT base on whisper,and some other stuff.
1. 简单的基于chatGPT的字幕翻译器，目前只支持SRT格式，并且除无译文且有序号的格式外，每句的原文和译文必须只有一行。
2. 基于whisper的语音转字幕。
3. 修复译文与原文错行。

## 使用
- 在yaml中配置自己api-key
- 在yaml配置要翻译的文件位置和其他参数
- 在Runner中将不需要的功能入口注释掉
- 运行SubtitleTranslatorApp
