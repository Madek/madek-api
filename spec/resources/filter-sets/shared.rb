require 'spec_helper'

shared_context :bunch_of_filter_sets do
  let :users_count do
    5
  end

  let :filter_sets_count do
    30
  end

  let :users do
    (1..users_count).map do
      FactoryGirl.create :user
    end
  end

  let :filter_sets do
    (1..filter_sets_count).map do
      FactoryGirl.create :filter_set,
                         responsible_user: users[rand(users_count)],
                         get_metadata_and_previews: (rand <= 0.8)
    end
  end
end
