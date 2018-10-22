require 'spec_helper'

ROUNDS = begin
           Integer(ENV['ROUNDS'].presence)
         rescue
           25
         end

shared_context :meta_datum_for_media_entry do |_ctx|
  let :media_resource do
    user = FactoryGirl.create(:user)
    FactoryGirl.create :media_entry, creator: user, responsible_user: user
  end
end

shared_context :meta_datum_for_random_resource_type do |_ctx|
  let :media_resource do
    user = FactoryGirl.create(:user)
    case
    when rand < 1.0 / 3
      FactoryGirl.create :media_entry, creator: user, responsible_user: user
    when rand < 1.0 / 2
      FactoryGirl.create :collection, creator: user, responsible_user: user
    else
      FactoryGirl.create :filter_set, creator: user, responsible_user: user
    end
  end

  def meta_datum(type)
    case media_resource
    when MediaEntry
      FactoryGirl.create "meta_datum_#{type}",
                         media_entry: media_resource
    when Collection
      FactoryGirl.create "meta_datum_#{type}",
                         collection: media_resource
    when FilterSet
      FactoryGirl.create "meta_datum_#{type}",
                         filter_set: media_resource
    end
  end
end
