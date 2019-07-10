require 'rails/all'
# require 'active_support/all'

module Madek
  class Application < Rails::Application
    config.eager_load = false

    config.paths['config/initializers'] << \
      Rails.root.join('datalayer', 'initializers')

    config.autoload_paths += [
      Rails.root.join('datalayer', 'lib'),
      Rails.root.join('datalayer', 'app', 'models'),
      Rails.root.join('datalayer', 'app', 'lib')
    ]

    config.paths['config/database'] = ['spec/config/database.yml']
  end
end

# ENV['DATABASE_URL']="Bogus"

# ActiveSupport::Dependencies.autoload_paths += [
#  Rails.root.join('datalayer', 'lib'),
#  Rails.root.join('datalayer', 'app', 'models'),
#  Rails.root.join('datalayer', 'app', 'lib'),
# ]

Rails.application.initialize!
