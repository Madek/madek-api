require "spec_helper"

ROUNDS = begin
    Integer(ENV["ROUNDS"].presence)
  rescue
    25
  end

shared_context :meta_datum_for_media_entry do |_ctx|
  let :media_resource do
    user = FactoryBot.create(:user)
    FactoryBot.create :media_entry, creator: user, responsible_user: user
  end
end

shared_context :meta_datum_for_random_resource_type do |_ctx|
  let :media_resource do
    user = FactoryBot.create(:user)
    case
    when rand < 1.0 / 2
      FactoryBot.create :media_entry, creator: user, responsible_user: user
    else
      FactoryBot.create :collection, creator: user, responsible_user: user
    end
  end

  def meta_datum(type)
    case media_resource
    when MediaEntry
      FactoryBot.create "meta_datum_#{type}",
        media_entry: media_resource
    when Collection
      FactoryBot.create "meta_datum_#{type}",
        collection: media_resource
    end
  end
end
