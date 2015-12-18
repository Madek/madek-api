require 'spec_helper'

shared_context :bunch_of_media_entries do
  let :users_count do
    5
  end

  let :media_entries_count do
    30
  end

  let :users do
    (1..users_count).map do
      FactoryGirl.create :user
    end
  end

  let :media_entries do
    (1..media_entries_count).map do
      FactoryGirl.create :media_entry,
                         responsible_user: users[rand(users_count)],
                         is_published: (rand <= 0.9),
                         get_metadata_and_previews: (rand <= 0.8),
                         get_full_size: (rand <= 0.3)
    end
  end

  let :collection do
    coll = FactoryGirl.create :collection
    media_entries.each do |me|
      if (rand <= 0.75)
        FactoryGirl.create :collection_media_entry_arc,
          collection: coll, media_entry: me
      end
    end
    coll
  end

end
