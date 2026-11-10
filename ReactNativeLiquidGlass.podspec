require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "ReactNativeLiquidGlass"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms     = { :ios => "26.0" }
  s.swift_version = "6.0"
  s.source       = { :git => "https://github.com/MohamedAbdElNaby/mohamed_abd_elnaby-react-native-liquid-glass.git", :tag => "v#{s.version}" }

  s.source_files = "ios/**/*.{h,m,mm,swift,cpp}"
  s.exclude_files = "ios/Tests/**/*"
  s.private_header_files = "ios/**/*.h"

  install_modules_dependencies(s)

  s.test_spec "Tests" do |t|
    t.source_files = "ios/Tests/*.swift"
    # The Swift-only test bundle links React's C++ static libs but not libc++ itself.
    t.libraries = "c++"
  end
end
