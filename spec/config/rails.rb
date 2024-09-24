require "rails/all"

module Madek
  class Application < Rails::Application
    config.eager_load = false

    config.paths["config/initializers"] << Rails.root.join("datalayer", "initializers")

    config.autoload_paths += [
      Rails.root.join("datalayer", "lib"),
      Rails.root.join("datalayer", "app", "models", "concerns"),
      Rails.root.join("datalayer", "app", "models"),
      Rails.root.join("datalayer", "app", "lib"),
    ]

    config.paths["config/database"] = ["spec/config/database.yml"]

    Rails.autoloaders.main.inflector.inflect("json" => "JSON")
  end
end

Rails.application.initialize!
