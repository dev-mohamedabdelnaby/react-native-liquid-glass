require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "ReactNativeLiquidGlass"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  # The iOS app's deployment target and Swift settings: its code is copied unchanged into ios/Catalog.
  s.platforms     = { :ios => "27.0" }
  s.swift_version = "5.0"
  s.source       = { :git => "https://github.com/dev-mohamedabdelnaby/react-native-liquid-glass.git", :tag => "v#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift,cpp}"
  s.exclude_files = "ios/Tests/**/*"
  s.private_header_files = "ios/**/*.h"

  # Set before install_modules_dependencies, which merges its settings into it.
  s.pod_target_xcconfig = {
    "SWIFT_DEFAULT_ACTOR_ISOLATION" => "MainActor",
    "SWIFT_APPROACHABLE_CONCURRENCY" => "YES",
    "SWIFT_UPCOMING_FEATURE_MEMBER_IMPORT_VISIBILITY" => "YES",
  }

  install_modules_dependencies(s)

  s.test_spec "Tests" do |t|
    t.source_files = "ios/Tests/*.swift"
    # The Swift-only test bundle links React's C++ static libs but not libc++ itself.
    t.libraries = "c++"
  end
end
