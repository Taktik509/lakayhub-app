//
//  ContentView.swift
//  LakayHub — Aplikasyon natif iOS
//
//  WKWebView ak sekirite wo nivo + senkronizasyon otomatik ak sit la
//

import SwiftUI
import WebKit

struct ContentView: View {
    var body: some View {
        LakayHubWebView()
            .ignoresSafeArea()
    }
}

struct LakayHubWebView: UIViewRepresentable {
    static let baseURL = "https://lakayhub.space"
    static let appVersion = 1

    func makeUIView(context: Context) -> WKWebView {
        // ---------- SEKIRITE WO NIVO ----------
        let config = WKWebViewConfiguration()
        config.preferences.javaScriptEnabled = true
        // Pa pèmèt aksè fichye lokal
        config.preferences.setValue(false, forKey: "allowFileAccessFromFileURLs")

        let webView = WKWebView(frame: .zero, configuration: config)
        webView.navigationDelegate = context.coordinator

        // User agent koutim
        webView.customUserAgent = (webView.value(forKey: "userAgent") as? String ?? "")
            + " LakayHub-iOS/\(Self.appVersion)"

        // Chaje sit la — SENKRONIZASYON OTOMATIK
        if let url = URL(string: Self.baseURL + "/?app=1") {
            webView.load(URLRequest(url: url))
        }
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    class Coordinator: NSObject, WKNavigationDelegate {
        // BLOKE navigasyon deyò lakayhub.space (anti-phishing)
        func webView(_ webView: WKWebView,
                     decidePolicyFor navigationAction: WKNavigationAction,
                     decisionHandler: @escaping (WKNavigationActionPolicy) -> Void) {
            if let host = navigationAction.request.url?.host,
               host == "lakayhub.space" || host.hasSuffix(".lakayhub.space") {
                decisionHandler(.allow)
            } else if navigationAction.navigationType == .linkActivated,
                      let url = navigationAction.request.url {
                // Louvri lyen ekstèn nan Safari
                UIApplication.shared.open(url)
                decisionHandler(.cancel)
            } else {
                decisionHandler(.allow)
            }
        }

        // BLOKE erè SSL — pa kontinye
        func webView(_ webView: WKWebView,
                     didReceive challenge: URLAuthenticationChallenge,
                     completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
            completionHandler(.performDefaultHandling, nil)
        }
    }
}

// ---------- SPLASH NATIF ak animasyon ----------
struct SplashView: View {
    @State private var scale: CGFloat = 0.3
    @State private var opacity: Double = 0
    @State private var isActive = false

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color(hex: "0A2472"), Color(hex: "1B5BD7")],
                           startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            if isActive {
                ContentView()
            } else {
                VStack(spacing: 16) {
                    // Logo kay oranj
                    ZStack {
                        RoundedRectangle(cornerRadius: 24)
                            .fill(Color(hex: "F97316"))
                            .frame(width: 110, height: 110)
                        Image(systemName: "house.fill")
                            .font(.system(size: 52))
                            .foregroundColor(.white)
                    }
                    .scaleEffect(scale)
                    .opacity(opacity)

                    Text("LakayHub")
                        .font(.system(size: 34, weight: .bold))
                        .foregroundColor(.white)
                    Text("Biznis ou. Lakay ou. Yon sèl Hub.")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.75))

                    // Loading dots
                    HStack(spacing: 8) {
                        ForEach(0..<3) { i in
                            Circle()
                                .fill(Color(hex: "F97316"))
                                .frame(width: 10, height: 10)
                        }
                    }
                    .padding(.top, 12)
                }
                .onAppear {
                    withAnimation(.spring(response: 0.8, dampingFraction: 0.6)) {
                        scale = 1.0
                        opacity = 1.0
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                        withAnimation { isActive = true }
                    }
                }
            }
        }
    }
}

extension Color {
    init(hex: String) {
        let h = hex.trimmingCharacters(in: .whitespacesAndNewlines)
        let s = h.hasPrefix("#") ? String(h.dropFirst()) : h
        var rgb: UInt64 = 0
        Scanner(string: s).scanHexInt64(&rgb)
        self.init(red: Double((rgb >> 16) & 0xFF) / 255,
                  green: Double((rgb >> 8) & 0xFF) / 255,
                  blue: Double(rgb & 0xFF) / 255)
    }
}
