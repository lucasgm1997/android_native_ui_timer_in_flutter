import BackgroundTasks
import Flutter
import UIKit
import UserNotifications

@main
@objc class AppDelegate: FlutterAppDelegate {
  private let CHANNEL = "com.example.native_timer/timer"
  private var timer: Timer?
  private var backgroundTaskIdentifier: UIBackgroundTaskIdentifier = .invalid
  private var timerDuration: Int = 0
  private var remainingTime: Int = 0
  private var isTimerRunning: Bool = false

  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)

    // Request notification permissions
    requestNotificationPermissions()

    // Setup method channel
    setupMethodChannel()

    // Register background task
    registerBackgroundTask()

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }

  private func requestNotificationPermissions() {
    UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) {
      granted, error in
      if granted {
        print("Notification permissions granted")
      } else {
        print("Notification permissions denied")
      }
    }
  }

  private func setupMethodChannel() {
    guard let controller = window?.rootViewController as? FlutterViewController else {
      return
    }

    let methodChannel = FlutterMethodChannel(
      name: CHANNEL, binaryMessenger: controller.binaryMessenger)

    methodChannel.setMethodCallHandler {
      [weak self] (call: FlutterMethodCall, result: @escaping FlutterResult) in
      switch call.method {
      case "startTimer":
        if let args = call.arguments as? [String: Any],
          let duration = args["duration"] as? Int
        {
          self?.startNativeTimer(duration: duration)
          result(true)
        } else {
          result(
            FlutterError(code: "INVALID_ARGUMENT", message: "Duration not provided", details: nil))
        }
      case "stopTimer":
        self?.stopNativeTimer()
        result(true)
      case "isTimerRunning":
        result(self?.isTimerRunning ?? false)
      default:
        result(FlutterMethodNotImplemented)
      }
    }
  }

  private func registerBackgroundTask() {
    if #available(iOS 13.0, *) {
      BGTaskScheduler.shared.register(
        forTaskWithIdentifier: "com.example.native_timer.background", using: nil
      ) { task in
        self.handleBackgroundTask(task: task as! BGAppRefreshTask)
      }
    }
  }

  @available(iOS 13.0, *)
  private func handleBackgroundTask(task: BGAppRefreshTask) {
    task.expirationHandler = {
      task.setTaskCompleted(success: false)
    }

    // Continue timer in background if running
    if isTimerRunning {
      scheduleBackgroundTask()
      task.setTaskCompleted(success: true)
    } else {
      task.setTaskCompleted(success: true)
    }
  }

  @available(iOS 13.0, *)
  private func scheduleBackgroundTask() {
    let request = BGAppRefreshTaskRequest(identifier: "com.example.native_timer.background")
    request.earliestBeginDate = Date(timeIntervalSinceNow: 1)

    try? BGTaskScheduler.shared.submit(request)
  }

  private func startNativeTimer(duration: Int) {
    stopNativeTimer()  // Stop any existing timer

    timerDuration = duration
    remainingTime = duration
    isTimerRunning = true

    // Start background task
    backgroundTaskIdentifier = UIApplication.shared.beginBackgroundTask { [weak self] in
      self?.stopNativeTimer()
    }

    // Schedule completion notification
    scheduleCompletionNotification(after: duration)

    // Start timer for foreground updates
    timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
      self?.updateTimer()
    }

    // Schedule background task for iOS 13+
    if #available(iOS 13.0, *) {
      scheduleBackgroundTask()
    }
  }

  private func stopNativeTimer() {
    isTimerRunning = false
    timer?.invalidate()
    timer = nil
    remainingTime = 0

    // Cancel scheduled notifications
    UNUserNotificationCenter.current().removeAllPendingNotificationRequests()

    // End background task
    if backgroundTaskIdentifier != .invalid {
      UIApplication.shared.endBackgroundTask(backgroundTaskIdentifier)
      backgroundTaskIdentifier = .invalid
    }
  }

  private func updateTimer() {
    remainingTime -= 1

    if remainingTime <= 0 {
      onTimerComplete()
    }
  }

  private func onTimerComplete() {
    stopNativeTimer()
    // Completion notification is already scheduled
  }

  private func scheduleCompletionNotification(after seconds: Int) {
    let content = UNMutableNotificationContent()
    content.title = "Timer Concluído!"
    content.body = "O timer foi finalizado com sucesso"
    content.sound = .default
    content.badge = 1

    let trigger = UNTimeIntervalNotificationTrigger(
      timeInterval: TimeInterval(seconds), repeats: false)
    let request = UNNotificationRequest(
      identifier: "timer_completion", content: content, trigger: trigger)

    UNUserNotificationCenter.current().add(request) { error in
      if let error = error {
        print("Error scheduling notification: \(error)")
      }
    }
  }

  override func applicationDidEnterBackground(_ application: UIApplication) {
    super.applicationDidEnterBackground(application)

    if isTimerRunning {
      if #available(iOS 13.0, *) {
        scheduleBackgroundTask()
      }
    }
  }

  override func applicationWillEnterForeground(_ application: UIApplication) {
    super.applicationWillEnterForeground(application)

    // Clear badge when app becomes active
    UIApplication.shared.applicationIconBadgeNumber = 0
  }
}
