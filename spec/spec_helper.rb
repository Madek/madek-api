require 'active_support/all'
ENV['RAILS_ENV'] = ENV['RAILS_ENV'].presence || 'test'

require 'faker'
require 'config/bundle'
require 'config/rails'
require 'config/database'
require 'config/web'

require 'shared/clients'
require 'pry'
require 'uuidtools'

RSpec.configure do |config|
  config.before :all do
    @spec_seed = \
      ENV['SPEC_SEED'].presence.try(:strip) || `git log -n1 --format=%T`.strip
    puts "SPEC_SEED #{@spec_seed} set env SPEC_SEED to force value"
    srand Integer(@spec_seed, 16)
  end
  config.after :all do
    puts "SPEC_SEED #{@spec_seed} set env SPEC_SEED to force value"
  end

  config.include FactoryGirl::Syntax::Methods
end
