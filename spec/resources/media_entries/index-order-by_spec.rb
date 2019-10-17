require 'spec_helper'
require Pathname(File.expand_path('..', __FILE__)).join('shared')

describe 'ordering media entries by created_at' do
  include_context :bunch_of_media_entries

  include_context :json_roa_client_for_authenticated_user do
    let :media_entries_relation do
      media_entries # force evaluation
      collection # force evaluation
      client.get.relation('media-entries')
    end

    def resource(order = nil)
      media_entries_relation.get('order' => order)
    end

    context "old style string desc/asc order attribute" do


      def media_entries_created_at(order = nil)
        # to_datetime.strftime('%Q').to_i => int with ms precision
        resource(order)
          .data['media-entries']
          .map { |me| MediaEntry.unscoped.find(me['id']) }
          .map { |me| me.created_at.to_datetime.strftime('%Q').to_i }
      end

      it 'defaults to :asc' do
        media_entries_created_at.each_cons(2) do |ca_pair|
          expect(ca_pair.first < ca_pair.second).to be true
        end
      end

      it 'ascending' do
        media_entries_created_at('asc').each_cons(2) do |ca_pair|
          expect(ca_pair.first < ca_pair.second).to be true
        end
      end

      it 'descending' do
        media_entries_created_at('desc').each_cons(2) do |ca_pair|
          expect(ca_pair.first > ca_pair.second).to be true
        end
      end
    end

  end

  context "ordering media-entries in a particular set" do

    context "response of ordering by the order attribute of the arc descending" do

      let :response do
        media_entries_relation.get(
          collection_id: collection.id,
          order: [ [:arc, :order, :desc],
                   [:arc, :created_at, :desc],
                   [:media_entry, :created_at, :desc]].to_json).response
      end

      it "is OK" do
        expect(response.status).to be== 200
      end

      describe "arcs" do

        let :arcs do
          response.body.with_indifferent_access[:arcs]
        end

        let :arcs_before_first_order_nil do
          arcs.take_while{|a| a[:order]}
        end

        it "are present" do
          expect(arcs).not_to be_empty
        end

        it "nils come last" do
          arcs_tail_order_nils = arcs.reverse.take_while{|a| a[:order].nil?}.reverse
          expect(arcs).to be== arcs_before_first_order_nil + arcs_tail_order_nils
        end

        it "non order nil arcs are ordered descending" do
          expect(arcs_before_first_order_nil).to be==
            arcs_before_first_order_nil.sort_by{|arc| arc[:order] ? arc[:order] : -1 }.reverse
        end

        describe "child media entries" do

          let :child_media_entries do
            response.body.with_indifferent_access["media-entries"]
          end

          it "are exactly ordered like the arcs" do
            expect(child_media_entries.map{|me| me[:id]}).to be==
              arcs.map{|arc| arc[:media_entry_id]}
          end
        end
      end
    end


    context "response of ordering by the time media entries are added to the set" do

      let :response do
        media_entries_relation.get(
          collection_id: collection.id,
          order: [ [:arc, :created_at, :asc],
                   [:media_entry, :created_at, :desc]].to_json).response
      end


      describe "arcs" do

        let :arcs do
          response.body.with_indifferent_access[:arcs]
        end


        it "arcs are ordered by creation date ascending" do
          expect(arcs).to be==  arcs.sort_by{|arc| arc[:created_at]}
        end
      end
    end


    context "a title for each of the entries" do

      let :meta_key_title do
        FactoryGirl.create :meta_key_title
      end

      let :titles do
        media_entries.map do |me|
          @meta_datum_text = FactoryGirl.create :meta_datum_text,
            media_entry: me, meta_key: meta_key_title
        end
      end

      before :each do
        # force initialization
        titles
      end

      # NOTE: meta_data should have ref to media_entry; so maybe we dont need the dance
      # with the additional meta_data fields in the response!

      context "response of ordering by metadatum string (title usually)" do

        let :response do
          media_entries_relation.get(
            collection_id: collection.id,
            order: [["MetaDatum::Text", meta_key_title.id, :asc]].to_json).response
        end

        it "has success http state" do
          expect(response.status).to be== 200
        end

        describe "arcs" do

          let :arcs do
            response.body.with_indifferent_access[:arcs]
          end

          it "media_entries are ordered by metadatum string" do
            media_entry_ids_from_response = response.body.with_indifferent_access["media-entries"].map{|me| me[:id]}
            # titles.filter{|t| media_entry_ids_from_response.include? t[:media_entry_id] }\
            #   .sort_by{|t| t[:string]}.map{|t| puts t[:string]}
            media_entry_ids_ordered_by_titles = titles \
              .filter{|t| media_entry_ids_from_response.include? t[:media_entry_id] }\
              .sort_by{|t| t[:string]}.map{|t| t[:media_entry_id]}

            expect(media_entry_ids_ordered_by_titles.to_a.length)
              .to eq media_entry_ids_from_response.to_a.length

            expect(media_entry_ids_ordered_by_titles.to_a.sort)
              .to be== media_entry_ids_from_response.to_a.sort
          end
        end
      end
    end
  end
end
