require 'pg_tasks'

def clean_db
  PgTasks.truncate_tables
end

RSpec.configure do |config|
  config.before(:each) do
    clean_db
  end
end
